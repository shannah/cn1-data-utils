# Ant Tasks for Automatically Generating DataMappers for project

This project includes a single custom ANT task that can be used to generate the "mappers"
for the DTO objects.  "Mappers" are classes that know how to convert DTO objects to and from
serializable graphs of Maps, Lists, Strings, and Primitives. 

* [GenerateMappers](src/ca/weblite/dto/ant/GenerateMappers.java) : Custom ANT task used to generate the mappers.

## Usage

~~~~
<taskdef name="genmappers" 
                 classname="com.prohire.dto.ant.GenerateMappers" 
                 classpath="path/to/DataMapperANTTasks.jar">
</taskdef>
<genmappers file="Mappers/src/ca/weblite/dto/Mappers.mirah"
    globalDataMapperContextName="dto-mappers"
    packageName="ca.weblite.dto"
    >
    <fileset dir="src" includes="**/*.java" excludes="**/enums/**"/>
</genmappers>
~~~~

This is used inside the "gen-mappers" task of the ProhireMovileAppDTO project [build.xml file](../build.xml).

## Build From Source

**Requires Netbeans 8.1**

~~~
ant install
~~~

This will build the `dist/DataMapperANTTasks.jar` file and copy it into the `lib` directory of the parent ProhireMovileAppDTO so that it will be used in that project's build.
