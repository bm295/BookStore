package com.bookstore.application.sequences;
public interface SequenceRepository { SequenceRecord getByKey(String key); long allocateSequence(SequenceRecord sequence); record SequenceRecord(String key,long currentValue){} }
