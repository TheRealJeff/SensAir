# SensAir
Air Quality Sensor Application for Android. This application interfaces with a 

### Identifier Naming Convention
It is important to implement a consistent naming convention throughout the whole code. Generally, the project will follow classic java-styled notation, where the first word is lowercase and each following word has its first letter in uppercase. For example,
```
    firstSecondThird
```

#### Class and Object Naming Convention
Classes shall be named with the first letter of every word capitalized. Objects shall be named with the same java notation, where the first word is the item type and the next words are what the item does. E.g.
```
    typeDescription
```
E.g.
```
    Button buttonGoToNext = new Button();
    DbHelper dbHelper = new DbHelper();
    EditText editTextUserName = new EditText();
```
#### Variables
Variables shall be named in all uppercase letters if they are final. Otherwise, the variable should follow the aforementioned java-styled notation. Variable names should be descriptive and clearly indicate what the variable is doing (except variables with a narrow scope, like in for loops). Some examples:
```
    final int MYVARIABLE = 5;
    int counter = 0;
    string userProfileId = user.getID();
```
#### Functions
Functions shall be named with the same java-styled notation. They must be descriptive and describe the functions purpose. Some examples:
```
    void getKey()
    double computeAverage()
    string getName()
```
