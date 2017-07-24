# Description

SimpleFIQL is a drastically simplified version of the Feed Item Query Language or short FIQL.
Its main purpose is to validate a Search-Query against an object.
This way you can filter out a few objects out of an massive array.
Another great plus is that it can be used in the Query of an URL, integrating perfectly with RESTful APIs.

# Syntax

Work in Progress

# Code Example

Let's say we have a massive array of persons and we want to filter all of them who's first name starts with the letter 'P'.  
The person class looks like this:

```java
public class Person {

    private String firstName;
	private String lastName;
	private Date birthDate;
	private float income;
	
	public Person(String firstName, String lastName, Date birthDate, float income) {
	
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.income = income;
	}
	
	public String getFirstName() {
	
		return this.firstName;
	}
	
	public String getLastName() {
	
		return this.lastName;
	}
	
	public Date getBirthDate() {
	
		return this.birthDate;
	}
	
	public float getIncome() {
	
		return this.income;
	}
}
```

The first thing we have to do is to mark all the Getters which we want to use in SimpleFIQL.

```java
public class Person {

    private String firstName;
	private String lastName;
	private Date birthDate;
	private float income;
	
	public Person(String firstName, String lastName, Date birthDate, float income) {
	
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.income = income;
	}
	
	@FIQLValue("first_name")
	public String getFirstName() {
	
		return this.firstName;
	}
	
	@FIQLValue("last_name")
	public String getLastName() {
	
		return this.lastName;
	}
	
	@FIQLValue("birth_date")
	public Date getBirthDate() {
	
		return this.birthDate;
	}
	
	@FIQLValue("income")
	public float getIncome() {
	
		return this.income;
	}
}
```

Then we just have to create an empty list, iterate through the original array, validate the elements and add all positive elements to the created list.

```java
List<Person> filtered = new ArrayList<>();

for(Person person : massiveArray) {

	try {
	
		if(FIQL.eval(person)) {
		
			filtered.add(person);
		}
	
	} catch(FIQLException exception) {
	
		exception.printStackTrace();
	}
}
```

# License

MIT License

Copyright (c) 2017 Ralph Niemitz

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.