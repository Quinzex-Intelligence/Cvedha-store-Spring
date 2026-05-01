package com.quinzex.service;
import com.quinzex.dto.CreateEbookRequest;
import com.quinzex.dto.EbookAdminResponse;
import com.quinzex.entity.Ebooks;
import com.quinzex.repository.EbookRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EbookAdminService implements IEbookAdminService {

    private final EbookRepo ebookRepo;
    private final S3Service s3Service;

    @Override
    @PreAuthorize("hasAuthority('GET_INACTIVE_BOOKS') or hasRole('SUPER_ADMIN')")
    public List<Ebooks> getInactiveEbooksList(Long cursor) {
        if (cursor == null) {
            return ebookRepo.findTop10ByActiveFalseOrderByBookIdAsc();
        } else {
            return ebookRepo.findTop10ByActiveFalseAndBookIdGreaterThanOrderByBookIdAsc(cursor);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('UPLOAD_BOOKS') or hasRole('SUPER_ADMIN')")
    @Transactional
    public String createEbooks(List<CreateEbookRequest> ebooksRequest) throws IOException {
        List<Ebooks> ebooksList = new ArrayList<>();
        for(CreateEbookRequest request : ebooksRequest){
            Ebooks ebook = new Ebooks();
            ebook.setBookName(request.getBookName());
            ebook.setBookAuthor(request.getBookAuthor());
            ebook.setBookDescription(request.getBookDescription());
            ebook.setBookPrice(request.getBookPrice());
            ebook.setTotalQuantity(request.getTotalQuantity());
            ebook.setAvailableQuantity(request.getTotalQuantity());
            ebook.setReservedQuantity(0);
            ebook.setActive(true);
            ebook.setBookCategory(request.getBookCategory());
            ebook.setBookPublishDate(request.getBookPublishDate());
            ebook.setLanguageCategory(request.getLanguageCategory());
            String key = s3Service.uploadFile(request.getCoverPhoto());
            ebook.setCoverPhoto(key);
            if ("PHYSICAL".equalsIgnoreCase(request.getBookCategory())) {
                ebook.setEbookPdfKey(null);
            }
            if("EBOOK".equalsIgnoreCase(request.getBookCategory())){
                if(request.getEbookPdf() == null || request.getEbookPdf().isEmpty()){
                    throw new IllegalArgumentException("PDF is required for ebook category");
                }
                String pdfKey = s3Service.uploadFilePDF(request.getEbookPdf());
                ebook.setEbookPdfKey(pdfKey);
            }
            ebooksList.add(ebook);
        }
         ebookRepo.saveAll(ebooksList);
        return ebooksList.size() +" uploaded successfully";

    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('INACTIVE_BOOKS')or hasRole('SUPER_ADMIN')")
    public String softDeleteEbooks(List<Long> ids) {
        int updatedCount = ebookRepo.deactivateBooks(ids);
        return updatedCount + " books deactivated successfully";
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ACTIVATE_BOOKS')or hasRole('SUPER_ADMIN')")
    public String activateEbooks(List<Long> ids) {
        int updatedCount = ebookRepo.activateBooks(ids);
        return updatedCount + " books activated successfully";
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('UPDATE_BOOKS')or hasRole('SUPER_ADMIN')")
    public String editBook(Long id, CreateEbookRequest editEbookRequest) throws IOException {
        Ebooks ebook = ebookRepo.findById(id).orElseThrow(()-> new IllegalArgumentException("Invalid book id " + id));
        ebook.setBookName(editEbookRequest.getBookName());
        ebook.setBookAuthor(editEbookRequest.getBookAuthor());
        ebook.setBookDescription(editEbookRequest.getBookDescription());
        ebook.setBookPrice(editEbookRequest.getBookPrice());
        ebook.setBookPublishDate(editEbookRequest.getBookPublishDate());
        ebook.setBookCategory(editEbookRequest.getBookCategory());
        ebook.setLanguageCategory(editEbookRequest.getLanguageCategory());
        if(editEbookRequest.getTotalQuantity() != null){
            int oldTotal = ebook.getTotalQuantity();
            int newTotal = editEbookRequest.getTotalQuantity();
            if (newTotal < ebook.getReservedQuantity()) {
                throw new RuntimeException(

                        "Total stock cannot be less than reserved quantity"
                );
            }
            int difference = newTotal - oldTotal;
                ebook.setTotalQuantity(newTotal);
                ebook.setAvailableQuantity(ebook.getAvailableQuantity() + difference);

        }
        if(editEbookRequest.getCoverPhoto() != null && !editEbookRequest.getCoverPhoto().isEmpty()){
            String existingKey = ebook.getCoverPhoto();

            if (existingKey == null) {
                existingKey = "ebooks/covers/" + UUID.randomUUID()
                        + "_" + editEbookRequest.getCoverPhoto().getOriginalFilename();
                ebook.setCoverPhoto(existingKey);
            }

            s3Service.uploadFileWithKey(editEbookRequest.getCoverPhoto(), existingKey);
        }

        if ("PHYSICAL".equalsIgnoreCase(editEbookRequest.getBookCategory())) {
            ebook.setEbookPdfKey(null);
        }
        if ("EBOOK".equalsIgnoreCase(editEbookRequest.getBookCategory())&& editEbookRequest.getEbookPdf() != null && !editEbookRequest.getEbookPdf().isEmpty()) {
            String existingPdfKey = ebook.getEbookPdfKey();
            if (existingPdfKey == null) {
                existingPdfKey = "ebooks/pdf/" + UUID.randomUUID()  + "_" + editEbookRequest.getEbookPdf().getOriginalFilename();
                ebook.setEbookPdfKey(existingPdfKey);
            }
            s3Service.uploadPdfFileWithKey(editEbookRequest.getEbookPdf(),existingPdfKey);
        }
     return "edited successfully";
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_BOOKS') or hasRole('SUPER_ADMIN')")
    public List<EbookAdminResponse> getActiveEbooks(Long cursor){
        List<Ebooks> ebooks;
        if(cursor == null){
            ebooks=ebookRepo.findTop10ByActiveTrueOrderByBookIdAsc();
        }else{
            ebooks=ebookRepo.findTop10ByActiveTrueAndBookIdGreaterThanOrderByBookIdAsc(cursor);
        }
        return ebooks.stream().map(this::convertToEbookAdminResponse).toList();
    }

private EbookAdminResponse convertToEbookAdminResponse(Ebooks ebook) {
        String coverPhotoUrl = null;
        String pdfUrl = null;

        if(ebook.getCoverPhoto()!= null){
            coverPhotoUrl =s3Service.generatePresignedUrl( ebook.getCoverPhoto());
        }
        if(ebook.getEbookPdfKey() != null){
            pdfUrl = s3Service.generatePresignedUrl( ebook.getEbookPdfKey());
        }
        return EbookAdminResponse.builder().bookId(ebook.getBookId())
                .bookName(ebook.getBookName())
                .bookAuthor(ebook.getBookAuthor())
                .bookDescription(ebook.getBookDescription())
                .bookPrice(ebook.getBookPrice())
                .bookPublishDate(ebook.getBookPublishDate())
                .coverPhotoUrl(coverPhotoUrl)
                .pdfUrl(pdfUrl)
                .bookCategory(ebook.getBookCategory())
                .languageCategory(ebook.getLanguageCategory())
                .totalQuantity(ebook.getTotalQuantity())
                .availableQuantity(ebook.getAvailableQuantity())
                .reservedQuantity(ebook.getReservedQuantity())
                .active(Boolean.TRUE.equals(ebook.getActive())).build();
}
}
