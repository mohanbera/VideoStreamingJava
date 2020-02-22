package com.example.demo.Controller;

import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;


@RestController
public class ThirdController
{
    static long part=1024*1024*3;


    @GetMapping("/test2/{id}")
    public void mySong(@PathVariable String id, HttpServletRequest rq, HttpServletResponse rs) throws IOException {
        File file=new File("/home/mrcoderider/IdeaProjects/demo (1)/demo/videos/"+id);

        String content_type= Files.probeContentType(Path.of("/home/mrcoderider/IdeaProjects/demo (1)/demo/videos/"+id));

        String range=rq.getHeader("Range");
        long length=Files.size(file.toPath());
        long start=0;
        long end=length;


        if(range!=null)
        {
            range=range.replace("bytes=","");
            String[] strings=range.split("-");
            start=Long.parseLong(strings[0]);
            if(strings.length>1)
            {
                end=Long.parseLong(strings[1]);
            }
        }

        long part1=Math.min(part, end-start);

        FileInputStream fileInputStream=new FileInputStream(file);
        rs.setContentType("video/webm");
        rs.addHeader("Accept-Ranges","bytes");
        //rs.setHeader("Content-Length",String.valueOf(part1));
        rs.setHeader("Content-Range", format("bytes %s-%s/%s", start, start+part1-1, length));
        rs.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        /*
        if(start>50)
        {
            fileInputStream.skip(start);
        }
         */
        BufferedOutputStream outputStream=new BufferedOutputStream(rs.getOutputStream());
        //IOUtils.write(bytes,outputStream);
        StreamUtils.copyRange(fileInputStream, outputStream, start, start + part1-1);
        outputStream.flush();
        fileInputStream.close();
    }

}
