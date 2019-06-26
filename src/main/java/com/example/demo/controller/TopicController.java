package com.example.demo.controller;

import com.example.demo.entity.Message;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import com.example.demo.model.MessageResponse;
import com.example.demo.repo.MessageRepo;
import com.example.demo.repo.TopicRepo;
import com.example.demo.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

@Controller
@RequestMapping("/topic")
public class TopicController {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    TopicRepo topicRepo;

    @Autowired
    MessageRepo messageRepo;


    @Autowired
    CommonService commonService;

    @RequestMapping(method = RequestMethod.POST, value = "/create")
    public String createTopic(Topic topic,
                              @RequestParam("file") MultipartFile multipartFile,
                              RedirectAttributes redirectAttributes
    ) throws IOException {
        if (multipartFile != null && !multipartFile.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + "." + multipartFile.getOriginalFilename();

            multipartFile.transferTo(new File(uploadPath + "/" + resultFilename));
            topic.setLogo(resultFilename);
        }
        topic.setId(commonService.generateId("topic"));

        Long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        Message message = new Message(commonService.generateId("topic"), topic.getId(), topic.getAuthor(), "system", timestamp, "m.room.create");
        topicRepo.save(topic);
        messageRepo.save(message);
        redirectAttributes.addAttribute("topicId", topic.getId());
        return "redirect:/topic/{topicId}";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{topicId}")
    public String renderTopicPage(@PathVariable String topicId, Model model,  @PageableDefault(size = 3) Pageable pageable) {

        Topic topic = topicRepo.findById(topicId);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        model.addAttribute("user", user);
        model.addAttribute("topic", topic);
        Page<Message> messages = messageRepo.getMessages(topicId, pageable);
        model.addAttribute("messages", messages);
        model.addAttribute("upload", uploadPath);
        return "topic";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/send_message")
    @ResponseBody
    public MessageResponse sendMessage(
            @RequestParam("topicId") String topicId,
            @RequestParam("sender") String sender,
            @RequestParam("content") String content
    ) {
        String messageId = commonService.generateId("topic");
        Long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        Message message = new Message(messageId, topicId, sender, content,timestamp, "m.room.message");

        messageRepo.save(message);
        MessageResponse messageResponse = new MessageResponse(content, sender, timestamp);
        return messageResponse;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/deleteTopic/{topicId}")
    public String deleteTopic(@PathVariable String topicId){

       Topic topic = topicRepo.findById(topicId);
       topicRepo.delete(topic);
       return "redirect:/";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/deleteMessage/{messageId}")
    public String deleteMessage(@PathVariable String messageId, RedirectAttributes redirectAttributes){

        Message message = messageRepo.findById(messageId);
        messageRepo.delete(message);
        redirectAttributes.addAttribute("topicId", message.getTopicsId());

        return "redirect:/topic/{topicId}";
    }



}
