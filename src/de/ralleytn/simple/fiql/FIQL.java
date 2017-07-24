/*
 * MIT License
 * 
 * Copyright (c) 2017 Ralph Niemitz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.ralleytn.simple.fiql;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Enthält die Methode {@link #eval(String, Map)}.
 * @author Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FIQL {

	private static final DateFormat FORMAT_D = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat FORMAT_T = new SimpleDateFormat("HH:mm:ss");
	private static final DateFormat FORMAT_DT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final DateFormat FORMAT_DTZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	/**
	 * Evaluiert eine Search-Query gegen ein Objekt.
	 * @param fiql Die Search-Query
	 * @param value Das Objekt, gegen das die Search-Query evaluiert werden soll
	 * @return {@code true}, wenn das Objekt der Search-Query entspricht, andernfalls {@code false}
	 * @throws FIQLException Wenn irgendetwas mit der Search-Query nicht stimmt.
	 * @since 1.0.0
	 */
	public static final boolean eval(String fiql, Object value) throws FIQLException {
		
		Map<Object, Object> map = new HashMap<>();
		
		for(Method method : value.getClass().getMethods()) {

			for(Annotation annotation : method.getAnnotations()) {
				
				if(annotation instanceof FIQLValue) {
					
					try {
						
						map.put(((FIQLValue)annotation).value(), method.invoke(value));
						
					} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
						
						throw new FIQLException(exception);
					}
					
					break;
				}
			}
		}
		
		return FIQL.eval(fiql, map);
	}
	
	/**
	 * Evaluiert eine Search-Query gegen ein Objekt.
	 * @param fiql Die Search-Query
	 * @param value Das Objekt, gegen das die Search-Query evaluiert werden soll
	 * @return {@code true}, wenn das Objekt der Search-Query entspricht, andernfalls {@code false}
	 * @throws FIQLException Wenn irgendetwas mit der Search-Query nicht stimmt.
	 * @since 1.0.0
	 */
	public static final boolean eval(String fiql, Map<?, ?> value) throws FIQLException {
		
		fiql = FIQL.escape(fiql);
		
		if(fiql.contains("(")) {
			
			if(!fiql.contains(")")) {
				
				throw new FIQLException("Unclosed '('!");
			}
			
			char[] tokens = fiql.toCharArray();
			Stack<Integer> scope = new Stack<Integer>();
			
			for(int index = 0; index < tokens.length; index++) {
				
				if(tokens[index] == '(') {
					
					scope.push(index);
					
				} else if(tokens[index] == ')') {
					
					if(!scope.isEmpty()) {
						
						int start = scope.pop();
						boolean result = FIQL.process(fiql.substring(start + 1, index),  value);
						
						StringBuilder builder = new StringBuilder(fiql);
						builder.delete(start, index + 1);
						builder.insert(start, Boolean.toString(result));
						
						return FIQL.eval(builder.toString(), value);
						
					} else {
						
						throw new FIQLException("One ')' too much!");
					}
				}
			}
			
		} else {
			
			return FIQL.process(fiql, value);
		}
		
		return false;
	}

	private static final boolean stringEquals(String string, String value, boolean ignoreCase) {
		
		return Pattern.compile("^" + FIQL.regexify(string) + "$", (ignoreCase ? Pattern.CASE_INSENSITIVE : 0)).matcher(value).matches();
	}

	private static final boolean stringContains(String string, String value, boolean ignoreCase) {

		return Pattern.compile(FIQL.regexify(string), (ignoreCase ? Pattern.CASE_INSENSITIVE : 0)).matcher(value).find();
	}

	private static final boolean processEquals(String expression, Map<?, ?> value, String operator, boolean ignoreCase) throws FIQLException, UnsupportedEncodingException, ParseException {
		
		String selector = expression.substring(0, expression.indexOf(operator));
		Object obj = value.get(selector);
		
		if(expression.length() == (selector.length() + operator.length()) && obj != null && obj instanceof String) {
			
			return "".equals(obj);
			
		} else {

			String base = expression.substring(expression.indexOf(operator) + operator.length());
			String val = URLDecoder.decode(base, "UTF-8");
			val.replace("\\n", "\n");
			val.replace("\\t", "\t");
			val.replace("\\r", "\r");
			
			if(obj == null) {
				
				return val.equals("null");
				
			} else if(obj instanceof String || obj instanceof Boolean) {
				
				if(base.contains("|")) {
					
					if(base.startsWith("[") && base.endsWith("]")) {
						
						String[] vals = base.substring(1, base.length() - 1).split("\\|");
						
						for(String v : vals) {
							
							if(FIQL.stringContains(URLDecoder.decode(v, "UTF-8"), obj.toString(), ignoreCase)) {
								
								return true;
							}
						}
					
					} else {
						
						String[] vals = base.split("\\|");
						
						for(String v : vals) {
							
							if(FIQL.stringEquals(URLDecoder.decode(v, "UTF-8"), obj.toString(), ignoreCase)) {
								
								return true;
							}
						}
					}
					
				} else {
					
					return (base.startsWith("[") && base.endsWith("]") && FIQL.stringContains(val.substring(1, val.length() - 1), obj.toString(), ignoreCase)) ||
						   FIQL.stringEquals(val, obj.toString(), ignoreCase);
				}
				
			} else if(obj instanceof Number) {
				
				double objVal = ((Number)obj).doubleValue();
				
				if(val.contains("~")) {
					
					String[] parts = val.split("~");
					
					if(parts.length != 2 || parts[0].isEmpty()) {
						
						throw new FIQLException("The range expression for the selector '" + selector + "' is incorrectly formatted!");
					}
					
					double left = Double.parseDouble(parts[0]);
					double right = Double.parseDouble(parts[1]);
					
					return objVal >= left && objVal <= right;
					
				} else {
					
					return objVal == Double.parseDouble(val);
				}
				
			} else if(obj instanceof Date) {
				
				if(val.contains("~")) {
					
					String[] parts = val.split("~");
					
					if(parts.length == 2 && !parts[0].isEmpty()) {
						
						long timeLeft = FIQL.parseDate(parts[0]);
						long timeRight = FIQL.parseDate(parts[1]);
						long time = ((Date)obj).getTime();
						
						return time >= timeLeft && time <= timeRight;
						
					} else {
						
						throw new FIQLException("Invalid time range for the selector '" + selector + "'!");
					}
					
				} else {
					
					return ((Date)obj).getTime() == FIQL.parseDate(val.substring(1));
				}
				
			} else if(obj instanceof List) {
				
				for(Object element : (List<?>)obj) {
					
					if(element == null && val.equals("null")) {
						
						return true;
						
					} else if(element instanceof String || element instanceof Boolean) {
						
						if(base.contains("|")) {
							
							if(base.startsWith("[") && base.endsWith("]")) {
								
								String[] vals = base.substring(1, base.length() - 1).split("\\|");
								
								for(String v : vals) {
									
									if(FIQL.stringContains(URLDecoder.decode(v, "UTF-8"), element.toString(), ignoreCase)) {
										
										return true;
									}
								}
							
							} else {
								
								String[] vals = base.split("\\|");
								
								for(String v : vals) {
									
									if(FIQL.stringEquals(URLDecoder.decode(v, "UTF-8"), element.toString(), ignoreCase)) {
										
										return true;
									}
								}
							}
							
						} else if((base.equals("*") && !element.toString().isEmpty()) ||
								  (base.startsWith("[") && base.endsWith("]") && FIQL.stringContains(val.substring(1, val.length() - 1), element.toString(), ignoreCase)) ||
								  (FIQL.stringEquals(val, element.toString(), ignoreCase))) {
							
							return true;
						}
						
					} else if(element instanceof Number) {
						
						double objVal = ((Number)element).doubleValue();
						
						if(val.contains("~")) {
							
							String[] parts = val.split("~");
							
							if(parts.length != 2 || parts[0].isEmpty()) {
								
								throw new FIQLException("The range expression for the selector '" + selector + "' is incorrectly formatted!");
							}
							
							double left = Double.parseDouble(parts[0]);
							double right = Double.parseDouble(parts[1]);
							
							return objVal >= left && objVal <= right;
							
						} else {
							
							return objVal == Double.parseDouble(val);
						}
						
					} else if(element instanceof Date) {
						
						if(val.contains("~")) {
							
							String[] parts = val.split("~");
							
							if(parts.length == 2 && !parts[0].isEmpty()) {
								
								long timeLeft = FIQL.parseDate(parts[0]);
								long timeRight = FIQL.parseDate(parts[1]);
								long time = ((Date)element).getTime();

								if(time >= timeLeft && time <= timeRight) {
									
									return true;
								}
								
							} else {
								
								throw new FIQLException("Invalid time range for the selector '" + selector + "'!");
							}
							
						} else if(((Date)element).getTime() == FIQL.parseDate(val)) {
							
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	private static final boolean processGreaterThanOrEqualTo(String expression, Map<?, ?> value) throws FIQLException, UnsupportedEncodingException, ParseException {
		
		String selector = expression.substring(0, expression.indexOf(">="));
		Object obj = value.get(selector);
		
		if(expression.length() == (selector.length() + 2) && obj != null && obj instanceof String) {
			
			return false;
			
		} else {
			
			String val = URLDecoder.decode(expression.substring(expression.indexOf(">=") + 2), "UTF-8");
			val.replace("\\n", "\n");
			val.replace("\\t", "\t");
			val.replace("\\r", "\r");
			
			       if(obj == null)           {return false;
			} else if(obj instanceof Number) {return ((Number)obj).doubleValue() >= Double.parseDouble(val);
			} else if(obj instanceof Date)   {return ((Date)obj).getTime() >= FIQL.parseDate(val);
			} else if(obj instanceof List) {
				
				for(Object element : (List<?>)obj) {

					if(element != null && (element instanceof Number && ((Number)element).doubleValue() >= Double.parseDouble(val)) && (element instanceof Date && ((Date)element).getTime() >= FIQL.parseDate(val))) {
							
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private static final boolean processSmallerThanOrEqualTo(String expression, Map<?, ?> value) throws FIQLException, UnsupportedEncodingException, ParseException {
		
		String selector = expression.substring(0, expression.indexOf("<="));
		Object obj = value.get(selector);
		
		if(expression.length() == (selector.length() + 2) && obj != null && obj instanceof String) {
			
			return false;
			
		} else {
			
			String val = URLDecoder.decode(expression.substring(expression.indexOf("<=") + 2), "UTF-8");
			val.replace("\\n", "\n");
			val.replace("\\t", "\t");
			val.replace("\\r", "\r");
			
			if(obj == null) {
				
				return false;
				
			} else if(obj instanceof Number) {
				
				return ((Number)obj).doubleValue() <= Double.parseDouble(val);
				
			} else if(obj instanceof Date) {
				
				return ((Date)obj).getTime() <= FIQL.parseDate(val);
				
			} else if(obj instanceof List) {
				
				for(Object element : (List<?>)obj) {

					if(element != null && (element instanceof Number && ((Number)element).doubleValue() <= Double.parseDouble(val)) && (element instanceof Date && ((Date)element).getTime() <= FIQL.parseDate(val))) {
							
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private static final boolean processSmallerThan(String expression, Map<?, ?> value) throws FIQLException, UnsupportedEncodingException, ParseException {
		
		String selector = expression.substring(0, expression.indexOf("<"));
		Object obj = value.get(selector);
		
		if(expression.length() == (selector.length() + 1) && obj != null && obj instanceof String) {
			
			return false;
			
		} else {
			
			String val = URLDecoder.decode(expression.substring(expression.indexOf("<") + 1), "UTF-8");
			val.replace("\\n", "\n");
			val.replace("\\t", "\t");
			val.replace("\\r", "\r");
			
			       if(obj == null)           {return false;
			} else if(obj instanceof Number) {return ((Number)obj).doubleValue() < Double.parseDouble(val);
			} else if(obj instanceof Date)   {return ((Date)obj).getTime() < FIQL.parseDate(val);
			} else if(obj instanceof List) {
				
				for(Object element : (List<?>)obj) {

					if(element != null && (element instanceof Number && ((Number)element).doubleValue() < Double.parseDouble(val)) && (element instanceof Date && ((Date)element).getTime() < FIQL.parseDate(val))) {
							
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private static final boolean processGreaterThan(String expression, Map<?, ?> value) throws FIQLException, UnsupportedEncodingException, ParseException {
		
		String selector = expression.substring(0, expression.indexOf(">"));
		Object obj = value.get(selector);
		
		if(expression.length() == (selector.length() + 1) && obj != null && obj instanceof String) {
			
			return false;
			
		} else {
			
			String val = URLDecoder.decode(expression.substring(expression.indexOf(">") + 1), "UTF-8");
			val.replace("\\n", "\n");
			val.replace("\\t", "\t");
			val.replace("\\r", "\r");
			
			       if(obj == null)           {return false;
			} else if(obj instanceof Number) {return ((Number)obj).doubleValue() > Double.parseDouble(val);
			} else if(obj instanceof Date)   {return ((Date)obj).getTime() > FIQL.parseDate(val);
			} else if(obj instanceof List) {
				
				for(Object element : (List<?>)obj) {

					if(element != null && (element instanceof Number && ((Number)element).doubleValue() > Double.parseDouble(val)) && (element instanceof Date && ((Date)element).getTime() > FIQL.parseDate(val))) {
							
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private static final boolean processOr(String expression, Map<?, ?> value) throws FIQLException {
		
		String[] parts = expression.split(",");
		
		for(int index = 0; index < parts.length; index++) {
			
			parts[index] = Boolean.toString(FIQL.process(parts[index], value));
		}
		
		boolean current = Boolean.parseBoolean(parts[0]);
		
		for(int index = 1; index < parts.length; index++) {
			
			current = current || Boolean.parseBoolean(parts[index]);
		}
		
		return current;
	}

	private static final boolean processAnd(String expression, Map<?, ?> value) throws FIQLException {
		
		String[] parts = expression.split(";");
		
		for(int index = 0; index < parts.length; index++) {

			parts[index] = Boolean.toString(FIQL.process(parts[index], value));
		}
		
		boolean current = Boolean.parseBoolean(parts[0]);
		
		for(int index = 1; index < parts.length; index++) {
			
			current = current && Boolean.parseBoolean(parts[index]);
		}
		
		return current;
	}

	private static final boolean process(String expression, Map<?, ?> value) throws FIQLException {
		
		try {
			
			// If-Else ist schneller als Switch
			
			       if(expression.contains(",")) {return FIQL.processOr(expression, value);
			} else if(expression.contains(";")) {return FIQL.processAnd(expression, value);
			} else {
				
				       if(expression.equals("true"))  {return true;
				} else if(expression.equals("false")) {return false;
				} else if(expression.contains("=="))  {return FIQL.processEquals(expression, value, "==", false);
				} else if(expression.contains("=#=")) {return FIQL.processEquals(expression, value, "=#=", true);
				} else if(expression.contains("!#=")) {return !FIQL.processEquals(expression, value, "!#=", true);
				} else if(expression.contains("!="))  {return !FIQL.processEquals(expression, value, "!=", false);
				} else if(expression.contains(">="))  {return FIQL.processGreaterThanOrEqualTo(expression, value);
				} else if(expression.contains("<="))  {return FIQL.processSmallerThanOrEqualTo(expression, value);
				} else if(expression.contains(">"))   {return FIQL.processGreaterThan(expression, value);
				} else if(expression.contains("<"))   {return FIQL.processSmallerThan(expression, value);
				} else {
					
					throw new FIQLException("Unknown operator!");
				}
			}
			
		} catch(UnsupportedEncodingException | NumberFormatException | ParseException exception) {
			
			throw new FIQLException(exception);
		}
	}

	private static final long parseDate(String expression) throws FIQLException, ParseException {
		
		Map<String, DateFormat> formats = new LinkedHashMap<>();
		formats.put("DTZ", FIQL.FORMAT_DTZ);
		formats.put("DT", FIQL.FORMAT_DT);
		formats.put("D", FIQL.FORMAT_D);
		formats.put("T", FIQL.FORMAT_T);
		
		expression = expression.toUpperCase();
		
		if(expression.startsWith("L")) {
			
			try {
				
				return Long.parseLong(expression.substring(1));
				
			} catch(NumberFormatException exception) {
				
				throw new FIQLException(exception);
			}
			
		} else {
			
			DateFormat format = null;
			int formatKeyLength = 0;
			
			for(String key : formats.keySet()) {
				
				if(expression.startsWith(key)) {
					
					format = formats.get(key);
					formatKeyLength = key.length();
					break;
				}
			}
				
			if(format == null) {
					
				throw new FIQLException("Date format is missing!");
			}
			
			return format.parse(expression.substring(formatKeyLength)).getTime();
		}
	}

	private static final String escape(String expression) {
		
		return expression.replace("\\(", "%28")
						 .replace("\\)", "%29")
						 .replace("\\;", "%3B")
						 .replace("\\,", "%2C")
						 .replace("\\==", "%3D%3D")
						 .replace("\\!=", "%21%3D")
						 .replace("\\<=", "%3C%3D")
						 .replace("\\>=", "%3E%3D")
						 .replace("\\<", "%3C")
						 .replace("\\>", "%3E")
						 .replace("\\=#=", "%3D%23%3D")
						 .replace("\\!#=", "%21%23%3D")
						 .replace("\\~", "%7E")
						 .replace("\\|", "%7C")
						 .replace("\\[", "%5B")
						 .replace("\\]", "%5D");
	}

	private static final String regexify(String string) {
		
		StringBuilder builder = new StringBuilder();
		boolean escape = false;
		char[] tokens = string.toCharArray();
		
		Map<Character, String> mapping = new HashMap<Character, String>();
		mapping.put('\n', "\\n");
		mapping.put('\t', "\\t");
		mapping.put('\r', "\\r");
		mapping.put('\b', "\\b");
		mapping.put('\f', "\\f");
		mapping.put('\0', "\\0");
		
		for(int index = 0; index < tokens.length; index++) {
			
			if(tokens[index] == '*') {
				
				if(index > 0 && tokens[index - 1] == '\\') {
					
					if(!escape) {
						
						escape = true;
						builder.append("\\Q");
					}
					
					builder.append(tokens[index]);
					
				} else {
					
					if(escape) {
						
						escape = false;
						builder.append("\\E");
					}
					
					builder.append("(.{1,})");
				}
				
			} else if(tokens[index] == '?') {
				
				if(index > 0 && tokens[index - 1] == '\\') {
					
					if(!escape) {
						
						escape = true;
						builder.append("\\Q");
					}
					
					builder.append(tokens[index]);
					
				} else {
					
					if(escape) {
						
						escape = false;
						builder.append("\\E");
					}
					
					builder.append(".");
				}
				
			} else if(tokens[index] == '\\') {
				
				if(index > 0 && tokens[index - 1] == '\\') {
					
					if(!escape) {
						
						escape = true;
						builder.append("\\Q");
					}
					
					builder.append('\\');
					tokens[index] = '\0'; // Wenn man mehrere '\' hintereinander hat und diese Zeile wäre nicht da, könnte es zu Problemen kommen.
				}
				
			} else if(mapping.containsKey(tokens[index])) {
				
				if(escape) {
					
					escape = false;
					builder.append("\\E");
				}
				
				builder.append(mapping.get(tokens[index]));
				
			} else {
				
				if(!escape) {
					
					escape = true;
					builder.append("\\Q");
				}
				
				builder.append(tokens[index]);
				
				if(escape && index == tokens.length - 1) {
					
					builder.append("\\E");
				}
			}
		}
		
		return builder.toString();
	}
}
