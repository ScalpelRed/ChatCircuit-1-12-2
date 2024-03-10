package com.scalpelred.chatcircuit;

public class ChatProcessor {

    private String name;
    public void setName(String name){
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void init(){

    }
    public String process(String prompt){
        return "";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChatProcessor) return ((ChatProcessor)obj).getName().equals(name);
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
