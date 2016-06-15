/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.dto.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;

/**
 * An ANT task that generates the DataMappers for all DTO objects.  This is used
 * inside the "gen-mappers" target inside the build.xml file for the DataMapperAppDTO project.
 * It uses the DataMappers library to automatically generate mappers for each DTO object.
 * 
 * See README.md file at root of project for example usage
 * 
 * 
 * @author shannah
 */
public class GenerateMappers extends Task {
    
    private ArrayList<FileSet> fileSets = new ArrayList<FileSet>();
    private File file;
    private String packageName;
    
    public void add(FileSet fileset) {
        fileSets.add(fileset);
    }
    
    public void setFile(File file) {
        this.file = file;
    }
    
    public File getFile() {
        return file;
    }
    
    public String globalDataMapperContextName;
    
    public void setGlobalDataMapperContextName(String name) {
        globalDataMapperContextName = name;
        
    }
    
    public String getGlobalDataMapperContextName() {
        return globalDataMapperContextName;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void execute() throws BuildException {
        if (file == null) {
            throw new BuildException("You must specify the 'file' attribute for the output mirah file to write the mappers");
        }
        File f = file;
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) {
                    throw new BuildException("Failed to create file "+f);
                }
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
        }
        
        StringBuilder mappersClass = new StringBuilder();
        StringBuilder mappersClassImports = new StringBuilder();
        String mappersClassName = file.getName().substring(0, file.getName().indexOf("."));
        if (globalDataMapperContextName != null) {
            if (packageName == null) {
                throw new BuildException("packageName required if you specify globalDataMapperContextName");
            }
            mappersClass
                    .append("class ").append(mappersClassName).append("\n")
                    .append("  def self.init\n")
                    .append("    if !@@initialized\n")
                    .append("      @@initialized = true\n");
        }
                        
        
        FileWriter writer = null;
        try {
            writer = new FileWriter(f);
            for (FileSet fs : fileSets) {
                Iterator<Resource> it = fs.iterator();
                File directory = fs.getDir();
                String lastPkgName = null;
                while (it.hasNext()) {
                    Resource nex = it.next();
                    String name = nex.getName();
                    if (!name.endsWith(".java")) {
                        continue;
                    }
                    System.out.println("Processing file "+name);
                    String pkgName = name.substring(0, name.indexOf(".")).replace('\\','.').replace('/', '.');
                    String className = pkgName.substring(pkgName.lastIndexOf(".")+1);
                    pkgName = pkgName.substring(0, pkgName.lastIndexOf("."));
                    if (!pkgName.equals(lastPkgName)) {
                        writer.write("\npackage "+pkgName+"\n");
                        writer.write("import ca.weblite.codename1.mapper.DataMapper\n");
                        lastPkgName = pkgName;
                    }
                    
                    writer.write("data_mapper "+className+":"+className+"Mapper\n");
                    if (globalDataMapperContextName != null) {
                        mappersClass.append("        currMapper = "+className+"Mapper.new\n");
                        mappersClass.append("        currMapper.setClassName('").append(pkgName).append(".").append(className).append("')\n");
                        mappersClass.append("        DataMapper.addGlobal('"+globalDataMapperContextName+"', currMapper)\n");
                        mappersClassImports.append("import ").append(pkgName).append(".").append(className).append("Mapper\n");
                    }
                }
            }
            if (globalDataMapperContextName != null) {
                mappersClass
                        .append("    end\n")
                        .append("  end\n")
                        .append("  def self.getInstance\n")
                        .append("    self.init\n")
                        .append("    DataMapper.getGlobal('").append(globalDataMapperContextName).append("')\n")
                        .append("  end\n")
                        .append("end\n");
                writer.write("\npackage "+packageName+"\n");
                writer.write(mappersClassImports.toString());
                writer.write(mappersClass.toString());
            }
            
            
                        
            
            
        } catch (IOException ex) {
            
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex){}
            }
        }
        
        
    }
    
}
