package com.example.springboot3.controller;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot3.common.Result;
import com.example.springboot3.entity.Files;
import com.example.springboot3.entity.User;
import com.example.springboot3.mapper.FileMapper;
import com.example.springboot3.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

//文件上传相关接口
@RestController
@RequestMapping ("/file")
public class FileController {
    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Resource
    private FileMapper fileMapper;

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        long size = file.getSize();


        //判断文件的目录是否生成，若不存在新建一个目录

        //定义一个文件唯一的标识码
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID=uuid+ StrUtil.DOT+type;
        String url;
        File uploadFile = new File(fileUploadPath + fileUUID);
        //判断文件的目录是否生成，若不存在新建一个目录
        if (!uploadFile.getParentFile().exists()){
            uploadFile.getParentFile().mkdirs();
        }
        //获取文件的md5,通过对比md5避免重复上传相同内容的文件
        String md5= SecureUtil.md5(file.getInputStream());
        //查询文件的md5是否存在
        Files files = getFileByMd5(md5);
        if (files!=null){
            url=files.getUrl();

        }else{
            //把获取的文件放进磁盘
            file.transferTo(uploadFile);
            url="http://localhost:8082/file/"+fileUUID;

        }






        Files saveFile = new Files();
        saveFile.setName(originalFilename);
        saveFile.setType(type);
        saveFile.setSize(size/1024);
        saveFile.setUrl(url);
        saveFile.setMd5(md5);
        fileMapper.insert(saveFile);


        //存储数据库
        return url;


    }
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        //根据文件识别唯一标识码
        File uploadFile = new File(fileUploadPath + fileUUID);
        ServletOutputStream os = response.getOutputStream();
        //设置输出流格式
        response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileUUID,"UTF-8"));
        response.setContentType("application/octet-stream");
        //读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();



    }


    private Files getFileByMd5(String md5){
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        List<Files> filesList = fileMapper.selectList(queryWrapper);
        return filesList.size()==0 ? null:filesList.get(0);


    }
    @DeleteMapping("/{id}")
    public  Result delete(@PathVariable Integer id){
        Files files = fileMapper.selectById(id);
        files.setIsDelete(true);
        fileMapper.updateById(files);
        return Result.success();
    }
    //批量删除
    @PostMapping("/del/batch")
    public  Result deleteBatch(@RequestBody List<Integer> ids){
        QueryWrapper<Files> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("id",ids);
        List<Files> files = fileMapper.selectList(queryWrapper);
        for (Files file: files ){
            file.setIsDelete(true);
            fileMapper.updateById(file);
        }

        return Result.success();
    }

    //分页查询接口
    @GetMapping("/page")

    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam (defaultValue = "")String name){

        QueryWrapper<Files> queryWrapper =new QueryWrapper<>();
        //查询未删除的记录
        queryWrapper.eq("is_delete",false);
        queryWrapper.orderByDesc("id");
        if(!"".equals(name)){
            queryWrapper.like("name",name);
        }

        return Result.success(fileMapper.selectPage(new Page<>(pageNum,pageSize),queryWrapper));


    }
    @PostMapping("/update")
    public Result update(@RequestBody Files files){
        return Result.success(fileMapper.updateById(files));
    }

}
