# Simple translator from C to ASM(assembly)
#### Final project in System-software class
## Build JAR PACKAGE
--------------------
(in ./sys-software-c2asm)
1. Compile .java file
```
$ javac -d ./bin ./src/*.java
```
2. Build a .jar package
```
$ jar cvf [target.jar] -C bin/ .
```
3. Update manifest file
```
$ jar uvfm [target.jar] manifest.mf
```
## Usage
```
java -jar [target.jar] [source.c] [output.asm]
```
## Demo ##
![image](https://github.com/ken90242/sys-software-c2asm/blob/master/demo.gif)