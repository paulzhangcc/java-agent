package com.paulzhangcc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Properties;

/**
 * @author paul
 * @description
 * @date 2018/8/7
 */
public class AgentApplication {
    public static void premain(String agentArgs, Instrumentation inst){

        System.out.println("**************[premain] ClassLoader="+Thread.currentThread().getContextClassLoader()+",thread="+Thread.currentThread().getName());

        System.out.println("Properties.class.getClassLoader()="+Properties.class.getClassLoader());
        Properties properties = new Properties();

        try {
            if (agentArgs != null && agentArgs.length()==0){
                properties.load(new FileInputStream(agentArgs));
            }else {
                properties.load(new FileInputStream("/opt/JavaAgent.properties"));
            }

        } catch (Exception e) {
            if (e instanceof FileNotFoundException){
                try {
                    properties.load(new FileInputStream("/opt/JavaAgent.properties"));
                } catch (IOException e1) {
                    System.out.println("**************[premain] how to user: java -javaagent:{1}=[{2}]  {1} is agent jar ,{2} is conf properties , default {2} is /opt/JavaAgent.properties ");
                    System.out.println("**************[premain] /opt/JavaAgent.properties for example sun/security/util/HostnameChecker=C:/Users/paul/Desktop/HostnameChecker.class");
                    e1.printStackTrace();
                }
            }else{
                e.printStackTrace();
            }

        }
        System.out.println("**************[premain] LOAD AGENT PROPERTIES CONTENT:"+properties);
        inst.addTransformer(new ClassFileTransformer(){
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                String property = properties.getProperty(className);
                if (property!=null){
                    System.out.println("**************[premain]:className="+className+",replace file is "+property);
                    byte[] fileBytes = getFileBytes(property);
                    if (fileBytes!=null){
                        System.out.println("**************[premain]:className="+className+",found classfileBuffer");
                        return fileBytes;
                    }else {
                        System.out.println("**************[premain]:className="+className+",not found classfileBuffer");
                        return null;
                    }
                }
                return null;
            }
        },true);

    }

    public static byte[] getFileBytes(String fileName){
        try {
            File file = new File(fileName);
            if (!file.exists()){
                return null;
            }
            long fileSize = file.length();
            FileInputStream fi = new FileInputStream(file);
            byte[] buffer = new byte[(int) fileSize];
            int offset = 0;
            int numRead = 0;
            while (offset < buffer.length
                    && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset != buffer.length) {
                return null;
            }
            fi.close();
            return buffer ;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
