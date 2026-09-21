package com.bookstore.infrastructure.files;
import com.bookstore.application.files.ReadSpecificLine; import java.io.*;
public final class LineFileReader implements ReadSpecificLine { public String readSpecificLine(String path,int lineNumber){if(path==null||path.isBlank())throw new RuntimeException("Invalid File Path"); try(var r=new BufferedReader(new FileReader(path))){for(int i=0;i<lineNumber;i++)if(r.readLine()==null)return null; return r.readLine();}catch(IOException e){throw new UncheckedIOException(e);}} }
