package jace.scholarwiki.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResult<T> {
    private List<T> items;
    private int total;
    private int page;
    private int size;
}
