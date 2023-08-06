package com.myfirstspringproject.Contoller;

import com.myfirstspringproject.Domain.Comment;
import com.myfirstspringproject.Domain.UserAccount;
import com.myfirstspringproject.Dto.CommentDto;
import com.myfirstspringproject.Dto.UserAccountPrincipal;
import com.myfirstspringproject.Repository.CommentRepository;
import com.myfirstspringproject.Repository.UserAccountRepository;
import com.myfirstspringproject.Service.CommentService;
import com.myfirstspringproject.Service.PaginationBarService;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class CommentsController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PaginationBarService paginationBarService;


    //강의에서는 검색후 데이터를 뿌려주는 거 및 그냥 데이터를 뿌려주는 것을 이 컨트롤러 하나로 다 구현했었다. 하지만 나는 검색기능을 구현 안한다.
//    @RequestMapping({"/", "/comments"})
    @GetMapping("/")
    public String comments(
            //자바 파일내에서 jpa쓸 떄 엔티티이름을 실제 db에 있는 명으로 해야하나? 아니면 엔티티클래스에 정의된 이름으로 해야하나? ... 아마 당연히 후자이긴 할듯.
            //sort는 어떤 엔티티의 createdDate인지 어떻게 알까? 아마 findAll(Pageable pageable) 구현되어있는 놈으로 알아서 찾나?
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.ASC) Pageable pageable,
            //@Embedded 방식으로 하면 createdDate말고 AuditingFields.createdDate라고 지정해주어야한다. //지금 다시 맵드슈퍼클래스로 바꿨ㅇ니 AuditingFields.삭제함.
            //아니 설마 Comment.auditingFields.createdDate인가;
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

//    ArticleRequest articleRequest vs ArticleDto 어떻게 사용했을까?
    @PostMapping("/save")
    public String saveComment(
            //들어오는 정보가 comment_content뿐이므로, 굳이 ModelAttribute로 받을 필욘 없을 것 같다.
            @RequestParam String content,
            @RequestParam Boolean isAffected,
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Autowired UserAccountRepository userAccountRepository
        )
    {
        //인증정보를 받아서 UserAccount 형으로 변환, 서비스로 넘겨주기
        //음, 여기(컨트롤러)에서 미리 엔티티로 변환하여 서비스로 넘겨준다는게 약간 불편하다. 모두 서비스에서 dto를 엔티티로 바꾸어 서비스->컨트롤러,서비스->뷰 했는데.
        //그래도 인증정보는 컨트롤러에서 받아야했기때문에 불가피하지않을까? -> UserAccountRepository 의존성을 갖고있어야하는데
        //컨트롤러에서 리포지토리 의존성을 갖는 경우는 여기서밖에 없을 예정이고 다른 컨트롤러메소드에선 잘 사용하지 않으므로 여기서만 의존성받자.
        // 컨트롤러->서비스->리포지토리 순, 디비에 등록하는 것이 최종 목적이므로 엔티티를 넘겨주는 것이 맞긴하다. 컨트롤러에선 서비스론 디티오로 넘겨주는게 맞는 것 같다. 아래코드는 컨트롤러에서 리포지접근중
//        UserAccount user = userAccountRepository.findByUserId(principal.getUserId()).map(UserAccount).
//                get();
        //서비스에서 엔티티로 변환하여 리포지토리로 넣어주는 기존방식으로 구현해보자.

        // /save로 매핑 후, redirection을 통해 /로 이동해야 자연스럽게 구현됨
        commentService.saveComment(content, isAffected, principal.getUsername());
        return "redirect:/";
    }

    //@PathVariable을 사용해야하나? 즉 /{commentId}/delete ,/{commentId}/delete 식으로 해결해야하지 않을까?

    @PostMapping("/{commentId}/delete")
    public ResponseEntity<String> deleteComment(
//            @PathVariable Long commentId, 이 부분이랑 파스롱부분 기억
            @PathVariable String commentId,
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @RequestBody String password
    ) {
        log.info(commentId); //{comment.id()}
        //해당 댓글의 삭제버튼을 누름 -> 즉 해당댓글의 아이디를 넘겨받는 로직을 구현해야함, 타임리프에서!
        //로그인했음에도 재확인용으로 비밀번호 입력받기 - 자바스크립트로하는 방법밖에 없을 듯. 따로 비밀번호 입력폼을 만들기는 거추장스러워보임
        //위 비밀번호 재확인하는 로직을 서비스에 구현하고 업데이트와 딜리트에서 사용 ... ? 그럼 또 principal을 넘겨야하잖아... 그냥 컨트롤러에 메소드하나 등록하면 안되나?
        //그렇다면 빈으로 잡힐까? 그러진 원치 않는데...
        //mvc대신, http body를 이용하여 통신해보자(RequestEntity) ... 이 방법은 ajax등을 사용해서 복잡하고, mvc로 못하는 건 없다,, 생각해보니
        String passwordRemovedQuotes = password.substring(1, password.length()-1);
        if (principal.getPassword().equals("{noop}"+passwordRemovedQuotes)) { //todo:패스워드 비교로직만 있었는데 아이디비교로직도 추가(0724원노트) -> 또는 강의처럼 deleteByIdAndUserAccount_UserId같은 로직을 구현하는것도 괜찮을 듯?
//            Long id = Long.parseLong(commentId.substring(1, commentId.length()-1));
//            java.lang.NumberFormatException: For input string: "{comment.id()"
//            Long id = Long.parseLong(commentId.substring(2, commentId.length())) 해결완료
            commentService.deleteComment(Long.parseLong(commentId));    //todo : 이 입력비번 검증부분이 문제다 if,else부분 주석처리하고 실행했더니 잘 삭제 된다.
                                                        //todo(V) : {noop}앞에 붙이는 것으로 해결, 암호화 되어있는 비밀번호를 가져올 떈 이렇게 추가해주면 된다.
            //1번가정) principal.getPassword()하는 부분이 문제다 -> 암호화 또는 로직 자체의 문제(겟패스워드가 이럴떄 쓰는 함수가 아니라던지)
            //2번가정) requestParam으로 String password를 제대로 못받았다.
            // return ResponseEntity.status(302).location(URI.create("/")).build(); //redirect로 삭제 후 댓글목록 반영까지 생각하여 구현해봤지만 실패
            return ResponseEntity.ok("댓글이 삭제되었습니다 commentId: "+Long.parseLong(commentId));
        } else {
            //error던지기, 이렇게 던지면 컨트롤
            //throw new IllegalArgumentException();
            //model.addAttribute("deleteError", "1");
            return ResponseEntity.badRequest().body("일치하지 않는 비밀번호입니다. from 컨트롤러");
        }
    }


    //비밀번호가 일치하는 지 확인하는 컨트롤러, 뷰로 리턴은 어떻게 해줘야하는가. 뷰에서 비밀번호검증과 폼ㅇ
    @PostMapping("/password-checker")
    public ResponseEntity<String> passwordChecker(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @RequestBody String password
    )
    {
        String passwordRemovedQuotes = password.substring(1, password.length()-1);
        //이렇게 따옴표 없애는 과정을 안하려면? 디티오를 써라. 알아서 매핑해줄테니. 원노트
        if(principal.getPassword().equals("{noop}"+passwordRemovedQuotes)){
            return ResponseEntity.ok("확인");
//            ok("확인").;
        }
        else{
            return ResponseEntity.badRequest().body("일치하지 않는 비밀번호입니다. 입력하신 비밀번호: " + passwordRemovedQuotes);
//            return ResponseEntity.badRequest().body("일치하지 않는 비밀번호입니다." + password); 확인을 해보니 ""로 감싸져있었음.
        }
    }


    //수정가능한 내용 : 콘텐트, 캐스캐이딩 여부 - form의 post로 들어옴
    //게시글을 수정 했다면, 게시글 폼을 받기 위한 get 요청과, 폼에서 수정 완료 후 보낼 post요청, 총 두개가 필요하지만, 댓글같은경우 자바스크립트를 통해 텍스트아리아를 수정하고 이 컨트롤러에서 그 데이터를 입력받아 한번에 처리한다.
    @PostMapping("/{commentId}/update")
    public String updateComment(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long commentId,
            @RequestParam String updatingContent,
            @RequestParam Boolean updatingIsAffected,
            ModelMap model
    ){
        commentService.updateComment(commentId, updatingContent, updatingIsAffected);
        return "redirect:/";
    }
}
