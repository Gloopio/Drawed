# Drawed App
The Drawing app is a demo app to demonstrate the usage of [Gloop](gloop.io). 

## Requiremens
- At least Android Studio 3.0
- Gloop libraries

## Build
- Create libs folder in app directory. 
- Copy gloopProcessor-VERSION.jar and gloopSDK-VERSION.jar into libs folder.
- Sync gradle and compile

# About using Gloop
In the following section the usage of the Gloop framework is described. 
The Drawed app should give you a short overview on how to use Gloop. 
A more detailed documentation can be found [hier](http://gloopio.github.io/Documentation/). 


## Setup libraries 

1) Copy the `gloopSDK-VERSION.jar` and `gloopProcessor-VERSION.jar` to libs folder of your project.

2) Add following dependencies to your `build.grade` file:

```groovy
compile fileTree(dir: 'libs', include: ['gloopSDK-*.jar'])
annotationProcessor fileTree(dir: 'libs', include: ['gloopProcessor-*.jar'])
```

3) Create an account on the [gloop.io](gloop.io) website. Login with the created account and create a new App. After the app was created you can see the ApiKey of the created App. Copy the ApiKey it is needed in your app project to authenticate your app.
The ApiKey can be added to your project by adding the following lines to your AndroidManifest.xml file under the application section:

```xml
<meta-data
       android:name="io.gloop.ApiKey"
       android:value="YOUR-API-KEY-GOES-HERE"/>
```
4) Initialize the Gloop framework by calling:

```java
new Gloop(this);
```
in your MainActivity within the `onCreate()` method. From this point on Gloop is ready to be used as local storage only. For online usage follow the next point otherwise skip the Authentication part.

## Authentication
For online usage you also have to register and login your client on the start of your application.

Register:

```java
boolean keepSignedIn = true;	// if set to true the user can be reloged in with the Gloop.loginWithRememberedUser() method
Gloop.register(USERNAME, PASSWORD, keepSignedIn);
```

Login:

```java
Gloop.login(USERNAME, PASSWORD);
```
or if keepSignedIn is set to true by simply calling:

```java
Gloop.loginWithRememberedUser();
```

If you want to log out a user to login another or create an account call 

```java
Gloop.logout();
```
This clears all local data of the logged in user.

## Object modeling
Now we will have a look on the objects. You just have to extend your object from the GloopObject and thats it. See example below.

```java
public class Board extends GloopObject {

    private boolean privateBoard = false;
    private boolean freezeBoard = false;
    private String name;
    private int color;

    private List<Line> lines = new ArrayList<>();
    
    // your getter and setters goes here
}
```

```java
public class Line extends GloopObject {

    private int color;
    private int brushSize;
    
    // your getter and setters goes here
}
```

Both objects do not contain any further methods or annotations it's just a normal Java data object.
Don't worry about other GloopObjects that are used within another GloopObject they are saved, deleted and loaded as well. In this example if we save a `Board` object then also the list of `Line` object is saved.

## Save an object
Saving an object can be done by calling the save method on your object. 

```java
Board board = new Board();
board.setName("Test");		
board.save();
```

## Delete an object
To delete a object call the delete method.

```java
board.delete();
```

## Querying objects. 
Lets say per example we want to query for all boards with the name "test" and that are private.

```java
Gloop.all(Board.class)
     .where()
	 .equalsTo("name", "Test")
	 .and()
	 .equalsTo("private", true)
	 .all();
```

Of course there are a lot of other operation provided for more complex queries. 

## Permissions 

Objects can either be **PRIVATE** or **PUBLIC** that means, that objects can be visible to the creator only or all other users of the same app. Only objects that are PUBLIC can be discovered with the `where()` queries. Private objects can only be found with the `where()` if the user that queries for is the creator of the object or access to this object is grated.

Objects can additionally be **READ** or **WRITE**. READ means that they can only be read but no changes on the object are saved and WRITE means that all changes to the object are allowed.

There is also a possibility to create Groups of Users that have different access privileges.

**Example:** A board object should be read only to a group of users and discoverable. The users should be able to add other users to the group.

```java
GloopGroup group = new GloopGroup();
group.setUser(Gloop.getOwner().getUserId(), PUBLIC | READ | WRITE);	// privileges of the users to change the group object.
group.save();

Board board = new Board();
board.setUser(group.getObjectId(), READ); 		// privileges of the users to change the board object
board.save();
```

**Another example:** Private board that should only be visible to the creator.

```java
Board board = new Board();
board.setUser(Gloop.getOwner().getUserId(), PRIVATE | READ);
board.save();
```

A more detailed documentation on permissions can be found [hier](http://gloopio.github.io/Documentation/). 



