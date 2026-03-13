package com.quinzex.service;

import com.quinzex.dto.EbookUserResponse;
import com.quinzex.entity.Ebooks;
import com.quinzex.repository.EbookRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EbookUserService implements IeBookUserService{

    private final EbookRepo  ebookRepo;
    private final S3Service s3Service;
    @Override
    public List<EbookUserResponse> getActiveEbooksList(Long cursor) {
        List<Ebooks> ebooks;
     if(cursor==null){
         ebooks = ebookRepo.findTop10ByActiveTrueOrderByBookIdAsc();
     }else{
         ebooks = ebookRepo.findTop10ByActiveTrueAndBookIdGreaterThanOrderByBookIdAsc(cursor);
     }
     return ebooks.stream().map(this::convertToUserResponse).toList();
    }
private EbookUserResponse convertToUserResponse(Ebooks ebooks){
String presignedUrl = null;
if(ebooks.getCoverPhoto()!=null){
    presignedUrl = s3Service.generatePresignedUrl(ebooks.getCoverPhoto());
}
return EbookUserResponse.builder()
        .bookId(ebooks.getBookId())
        .bookName(ebooks.getBookName())


        .bookAuthor(ebooks.getBookAuthor())
        .bookDescription(ebooks.getBookDescription())
        .bookPrice(ebooks.getBookPrice())
        .bookPublishDate(ebooks.getBookPublishDate())
        .coverPhotoUrl(presignedUrl)
        .bookCategory(ebooks.getBookCategory())
        .languageCategory(ebooks.getLanguageCategory())
        .inStock(ebooks.getAvailableQuantity() != null
                && ebooks.getAvailableQuantity() > 0).build();
}

@Override
    public List<EbookUserResponse> searchBooks(String keyword, Long cursor){
        List<Ebooks> ebooks = ebookRepo.searchActiveBooksWithCursor(keyword, cursor);
        return ebooks.stream().map(this::convertToUserResponse).toList();
    }

    @Override
    public List<EbookUserResponse> getBooksByLanguage(String language, Long cursor){

        List<Ebooks> ebooks = ebookRepo.findBooksByLanguageWithCursor(language, cursor);

        return ebooks.stream()
                .map(this::convertToUserResponse)
                .toList();
    }

    @Override
    public List<EbookUserResponse> getBooksByCategory(String category, Long cursor) {

        List<Ebooks> ebooks = ebookRepo.findBooksByCategoryWithCursor(category, cursor);

        return ebooks.stream()
                .map(this::convertToUserResponse)
                .toList();
    }
}
