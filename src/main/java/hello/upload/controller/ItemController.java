package hello.upload.controller;

import hello.upload.domain.Item;
import hello.upload.domain.ItemRepository;
import hello.upload.domain.UploadFile;
import hello.upload.file.FileStore;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

  static {
    System.out.println("=== ItemController Loaded ===");
  }

  private final ItemRepository itemRepository;
  private final FileStore fileStore;

  @GetMapping(value = "/items/new")
  public String newItem(@ModelAttribute ItemForm form) {
    log.info("form={}", form);
    log.info(("""
        fsldhfosdhfsoifhsfoish"""));
    return "item-form";
  }

  @PostMapping("items/new")
  public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes)
      throws IOException {
    UploadFile uploadFile = fileStore.storeFile(form.getAttachFile());
    List<UploadFile> uploadFiles = fileStore.storeFiles(form.getImageFiles());

    //save
    Item item = new Item();
    item.setItemName(form.getItemName());
    item.setAttachFile(uploadFile);
    item.setImageFiles(uploadFiles);
    itemRepository.save(item);

    redirectAttributes.addAttribute("itemId", item.getId());
    return "redirect:/items/{itemId}";
  }

  @GetMapping("/items/{id}")
  public String items(@PathVariable Long id, Model model) {
    Item item = itemRepository.findById(id);
    model.addAttribute("item", item);
    return "item-view";
  }

  @ResponseBody
  @GetMapping("/images/{filename}")
  public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
    return new UrlResource("file:" + fileStore.getFullPath(filename));
  }

  @GetMapping("/attach/{itemId}")
  public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId)
      throws MalformedURLException {
    Item item = itemRepository.findById(itemId);
    String storeFileName = item.getAttachFile()
                               .getStoreFileName();
    String uploadFileName = item.getAttachFile()
                                .getUploadFileName();

    UrlResource urlResource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));
    log.info("uploadFileName={}", uploadFileName);

    //deal with language issue
    String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
    String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

    return ResponseEntity.ok()
                         .header("Content-Disposition", contentDisposition)
                         //application/octet-stream means to treat it as a binary file,
                         //most browsers will treat it as a downloadable object.
                         .header("Content-Type", "application/octet-stream")
                         .body(urlResource);
  }

}
