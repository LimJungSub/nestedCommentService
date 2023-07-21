package com.myfirstspringproject.Service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class PaginationBarService {
    public static final int barLength = 5;

    public int getSize(){
        return barLength;
    }

    public List<Integer> returnNavList(int currentPage, int totalPage){

        int startNum = Math.max(currentPage - barLength/2, 0);
        int endNum = Math.min(startNum + barLength/2, totalPage);

        //원시타입이므로 박스드 필요
        return IntStream.range(startNum, endNum).boxed().toList();
    }
}
