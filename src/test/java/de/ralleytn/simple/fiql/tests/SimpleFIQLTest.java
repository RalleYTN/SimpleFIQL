package de.ralleytn.simple.fiql.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SimpleFIQLTest {

	private static final List<Map<String, Object>> ELEMENTS_ABSTRACT = new ArrayList<>();
	private static final List<TestObject> ELEMENTS = new ArrayList<>();
	private static final String EXAMPLE_DB = "de/ralleytn/simple/fiql/tests/example.db";
	private static final String SEPARATOR = "%NEW_RECORD%";
	
	@BeforeAll
	static void loadDB() throws IOException {
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(SimpleFIQLTest.class.getClassLoader().getResourceAsStream(EXAMPLE_DB), StandardCharsets.UTF_8))) {
			
			String line = null;
			boolean readDefinition = true;
			List<String> definition = new ArrayList<>();
			Map<String, Object> elementMap = new HashMap<>();
			TestObject element = new TestObject();
			int index = 0;
			
			while((line = reader.readLine()) != null) {
				
				if(line.equals(SEPARATOR)) {
					
					if(readDefinition) {
						
						readDefinition = false;
						
					} else {
						
						index = 0;
						ELEMENTS_ABSTRACT.add(elementMap);
						ELEMENTS.add(element);
						
						elementMap = new HashMap<>();
						element = new TestObject();
					}
					
				} else {
					
					if(readDefinition) {
						
						definition.add(line);
						
					} else {
						
						elementMap.put(definition.get(index), line);
						
						// TODO
						// ==== 14.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
						// Add the values for the TestObject instance.
						// ====
						
						index++;
					}
				}
			}
		}
	}
	
	@Test
	void testEvalObject() {
		
		ELEMENTS_ABSTRACT.get(0).forEach((key, val) -> System.out.println(key + ": " + val));
	}
	
	@Test
	void testEvalIteratable() {
		
		
	}
	
	@Test
	void testEvalMap() {
		
		
	}
}
