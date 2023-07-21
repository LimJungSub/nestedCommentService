package com.myfirstspringproject.Contoller;

import com.myfirstspringproject.Domain.Comment;
import com.myfirstspringproject.Dto.CommentDto;
import com.myfirstspringproject.Service.CommentService;
import com.myfirstspringproject.Service.PaginationBarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class CommentsController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PaginationBarService paginationBarService;

    //강의에서는 검색후 데이터를 뿌려주는 거 및 그냥 데이터를 뿌려주는 것을 이 컨트롤러 하나로 다 구현했었다. 하지만 나는 검색기능을 구현 안한다.
    @RequestMapping({"/", "/comments"})
    public String comments(
            //자바 파일내에서 jpa쓸 떄 엔티티이름을 실제 db에 있는 명으로 해야하나? 아니면 엔티티클래스에 정의된 이름으로 해야하나? ... 아마 당연히 후자이긴 할듯.
            //sort는 어떤 엔티티의 createdDate인지 어떻게 알까? 아마 findAll(Pageable pageable) 구현되어있는 놈으로 알아서 찾나?
            @PageableDefault(size = 10, sort = "AuditingFields.createdDate", direction = Sort.Direction.ASC) Pageable pageable,
            //@Embedded 방식으로 하면 createdDate말고 AuditingFields.createdDate라고 지정해주어야한다.
            //음 이렇게 sort=createdDate하면 엔티티쪽정렬구현 필요없이 알아서 갖고와주는거아니였나?
            ModelMap modelMap
    )
    {
        //서비스에서 다 dto로 바꿔서 컨트롤러에게 줌
        Page<CommentDto> list = commentService.getComments(pageable);
        List<Integer> paginationBar = paginationBarService.returnNavList(pageable.getPageNumber(), pageable.getPageSize());
        modelMap.addAttribute("paginationBar",paginationBar);
        modelMap.addAttribute("commentList",list);
        return "/index";
    }
}
