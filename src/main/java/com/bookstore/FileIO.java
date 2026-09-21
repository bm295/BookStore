package com.bookstore;
import com.bookstore.application.files.ReadSpecificLine; import com.bookstore.infrastructure.files.LineFileReader;
public final class FileIO { private final ReadSpecificLine reader; public FileIO(){this(new LineFileReader());} public FileIO(ReadSpecificLine reader){this.reader=reader;} public String readSpecificLine(String path,int line){return reader.readSpecificLine(path,line);} }
