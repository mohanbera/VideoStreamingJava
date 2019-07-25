package com.example.demo;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resources;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.nio.file.StandardOpenOption.READ;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class controller
{
    public static  Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");

    @RequestMapping("/")
    public String index()
    {
        return "index";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public void hello(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        PrintWriter out=res.getWriter();
        Cookie cookie0=new Cookie("MOHAN","10000");
        res.addCookie(cookie0);
        res.addHeader("Content-Type","text/html");
        Cookie[] cookies=req.getCookies();
        if( cookies != null ) {
            out.println("<h2> Found Cookies Name and Value</h2>");

            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                out.print("Name : " + cookie.getName( ) + ",  ");
                out.print("Value: " + cookie.getValue( ) + " <br/>");
            }
        } else {
            out.println("<h2>No cookies founds</h2>");
        }
    }

    @RequestMapping("/me")
    @ResponseBody
    public void me(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        Cookie[] cookies=req.getCookies();
        PrintWriter out=res.getWriter();
        String name=null;
        if(cookies!=null)
        {
            name=cookies[1].getName();
        }

        if(name!=null)
        {
            out.println("Welcome "+name);
        }
        else
        {
            out.println("Please give me your name");
        }
    }
    @RequestMapping("/text")
    @ResponseBody
    public void text(HttpServletRequest rq,HttpServletResponse rs) throws IOException {
        //rs.resetBuffer();
        var tika=new Tika();
        var file=new File("/home/immohan/IdeaProjects/demo/src/main/resources/static/pages/Limitless (2011).mp4");

        var range=rq.getHeader("Range");
        long length=file.length();
        int start=0;
        long end=length-1;

        boolean bool1=false;
        if(range!=null)
        {
            bool1=true;
            range=range.replace("bytes=","");
            String[] strings=range.split("-");
            start=Integer.parseInt(strings[0]);
            if(strings.length>1)
            {
                end=Integer.parseInt(strings[1])-1;
            }
            System.out.println(range+" "+start+" "+end);
        }

        System.out.println(tika.detect(file));
        FileInputStream fileInputStream=new FileInputStream(file);
        BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
        rs.setContentType("video/mp4");
        rs.addHeader("Accept-Ranges","bytes");
        rs.setHeader("Content-Length",String.valueOf(length));
        rs.setHeader("Content-Range", format("bytes %s-%s/%s", start, end, length));
        rs.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        BufferedOutputStream outputStream=new BufferedOutputStream(rs.getOutputStream());

        int num1=bufferedInputStream.read();
        if(bool1 && start>0)
        {
            bufferedInputStream.skipNBytes(start);
        }
        while (num1>-1)
        {
            outputStream.write(num1);
            num1=bufferedInputStream.read();
        }
        bufferedInputStream.close();
        System.out.println("FINISHED");
    }

    @RequestMapping("/mySong")
    @ResponseBody
    public void mySong(HttpServletRequest rq,HttpServletResponse rs) throws IOException {
        var tika=new Tika();
        var file=new File("/home/immohan/IdeaProjects/demo/src/main/resources/static/pages/jogi.MKV");

        System.out.println("MYSONG");
        var range=rq.getHeader("Range");
        int length=(int)(Files.size(file.toPath()));
        int start=0;
        int end=length-10000;

        if(range!=null)
        {
            range=range.replace("bytes=","");
            String[] strings=range.split("-");
            start=Integer.parseInt(strings[0]);
            if(strings.length>1)
            {
                end=Integer.parseInt(strings[1])-10000;
            }
            System.out.println(range+" "+start+" "+end);
        }

        System.out.println(tika.detect(file));
        FileInputStream fileInputStream=new FileInputStream(file);
        RandomAccessFile randomAccessFile=new RandomAccessFile(file,"rw");
        byte[] bytes=new byte[100000000];
        randomAccessFile.readFully(bytes,0,length);
        rs.setContentType("video/webm");
        rs.addHeader("Accept-Ranges","bytes");
        rs.setHeader("Content-Length",String.valueOf(length));
        rs.setHeader("Content-Range", format("bytes %s-%s/%s", start, end, length));
        rs.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        /*
        if(start>50)
        {
            fileInputStream.skip(start);
        }
         */
        BufferedOutputStream outputStream=new BufferedOutputStream(rs.getOutputStream());
        IOUtils.write(bytes,outputStream);
        outputStream.flush();
        fileInputStream.close();
        System.out.println("FINISHED");
    }


    @RequestMapping(value = "/videos", method = GET)
    @ResponseBody
    public final ResponseEntity<InputStreamResource>
    retrieveResource(HttpServletRequest req,HttpServletResponse res) throws Exception {

        long rangeStart=0;
        File file=new File("/home/immohan/IdeaProjects/demo/src/main/resources/static/pages/kabhi_yaadon_mein.mp4");
        long contentLenght = file.length();//you must have it somewhere stored or read the full file size
        long rangeEnd=file.length();

        String range=req.getHeader("Range");
        if(range!=null)
        {
            rangeStart = Long.parseLong((range.replace("bytes=","").split("-")[0]));//parse range header, which is bytes=0-10000 or something like that
            rangeEnd = Long.parseLong(range.replace("bytes=","").split("-")[0]);//parse range header, which is bytes=0-10000 or something like that
        }

        InputStream inputStream = new FileInputStream(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(Files.probeContentType(file.toPath())));
        headers.set("Accept-Ranges", "bytes");
        headers.set("Content-Transfer-Encoding", "binary");
        headers.set("Content-Length", String.valueOf(contentLenght));

//if start range assume that all content
        if (rangeStart == 0) {
            return new ResponseEntity<>(new InputStreamResource(inputStream), headers, OK);
        } else {
            headers.set("Content-Range", format("bytes %s-%s/%s", rangeStart, rangeEnd, contentLenght));
            return new ResponseEntity<>(new InputStreamResource(inputStream), headers, PARTIAL_CONTENT);
        }
    }


    @RequestMapping("/test123")
    public StreamingResponseBody handleRequest () {

        return new StreamingResponseBody() {
            @Override
            public void writeTo (OutputStream out) throws IOException {
                for (int i = 0; i < 1000; i++) {
                    out.write((Integer.toString(i) + " - ")
                            .getBytes());
                    out.flush();
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }


    @RequestMapping("/testVideo")
    public FileInputStream hiha () throws FileNotFoundException {

        File file=new File("/home/immohan/IdeaProjects/demo/src/main/resources/static/pages/jogi.MKV");
        FileInputStream fileInputStream=new FileInputStream(file);

        return fileInputStream;

        }
    }


