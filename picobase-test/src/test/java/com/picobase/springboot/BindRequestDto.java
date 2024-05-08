package com.picobase.springboot;

import java.util.Arrays;
import java.util.List;

public class BindRequestDto {
    List<String> name;
    String age;
    List<String> tags1;
    List<String> tags2;

    List<Integer> int2;

    Integer[] int3;

    int[] int4;



    public List<String> getName() {
        return name;
    }

    public BindRequestDto setName(List<String> name) {
        this.name = name;
        return this;
    }

    public String getAge() {
        return age;
    }

    public BindRequestDto setAge(String age) {
        this.age = age;
        return this;
    }

    public List<String> getTags1() {
        return tags1;
    }

    public BindRequestDto setTags1(List<String> tags1) {
        this.tags1 = tags1;
        return this;
    }

    public List<String> getTags2() {
        return tags2;
    }

    public BindRequestDto setTags2(List<String> tags2) {
        this.tags2 = tags2;
        return this;
    }

    public List<Integer> getInt2() {
        return int2;
    }

    public BindRequestDto setInt2(List<Integer> int2) {
        this.int2 = int2;
        return this;
    }

    public Integer[] getInt3() {
        return int3;
    }

    public BindRequestDto setInt3(Integer[] int3) {
        this.int3 = int3;
        return this;
    }

    public int[] getInt4() {
        return int4;
    }

    public BindRequestDto setInt4(int[] int4) {
        this.int4 = int4;
        return this;
    }

    @Override
    public String toString() {
        return "BindRequestDto{" +
                "name=" + name +
                ", age='" + age + '\'' +
                ", tags1=" + tags1 +
                ", tags2=" + tags2 +
                ", int2=" + int2 +
                ", int3=" + Arrays.toString(int3) +
                ", int4=" + Arrays.toString(int4) +
                '}';
    }
}
