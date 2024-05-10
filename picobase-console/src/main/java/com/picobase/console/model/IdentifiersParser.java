package com.picobase.pocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentifiersParser {
    private List<Identifier> columns;
    private List<Identifier> tables;

}