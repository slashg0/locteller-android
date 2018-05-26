# LocTeller 
### for Android

A primitive Android app to get current GPS location, with a customizable accuracy threshold. There are existing apps that do precisely the same, maybe better, available on Google Play. I made this in a few hours just for fun.

The app doesn't use internet at any point and is just a tool to get raw *latitude, longitude and altitude* coordinates from the device's GPS hardware. You can set a threshold for accuracy

How it works
------------
* The app has only one screen with the conventional `FloatingActionButton` based layout.
* To get the GPS data, **just press the big, round button**.
* By default, the app aims to get coordinates with a 5 meter `accuracy`\*.
* Enter the accuracy in the first text field as a number (in metres) and click *the button* to get data.
* The output fields are editables ones, to allow easy copying of each parameter.

\* The `accuracy` setting in the app follows this definition: [source](https://developer.android.com/reference/android/location/Location.html#getAccuracy() "Android's Location class")
>...the estimated horizontal accuracy of this location, radial, in meters.

>We define horizontal accuracy as the radius of 68% confidence. In other words, if you draw a circle centered at this location's latitude and longitude, and with a radius equal to the accuracy, then there is a 68% probability that the true location is inside the circle. 

Note
----
The accuracy of coordinates is subject to quality of hardware. The app or the developer do not guarantee legitimacy of data displayed in the device.

