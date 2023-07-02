package com.example.springboot3.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot3.common.Constants;
import com.example.springboot3.common.Result;
import com.example.springboot3.controller.dto.UserDTO;
import com.example.springboot3.entity.User;
import com.example.springboot3.mapper.UserMapper;
import com.example.springboot3.service.IUserService;

import com.example.springboot3.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

        @Autowired
        private IUserService userService;
        //登录
    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO){
      String username = userDTO.getUsername();
      String password = userDTO.getPassword();
      if(StrUtil.isBlank(username)|| StrUtil.isBlank(password)){
          return Result.error(Constants.code_400,"参数错误");
      }
         UserDTO dto= userService.login(userDTO);

        return Result.success(dto);
    }

    //注册
        @PostMapping("/register")
        public Result register(@RequestBody UserDTO userDTO){
                String username = userDTO.getUsername();
                String password = userDTO.getPassword();
                if(StrUtil.isBlank(username)|| StrUtil.isBlank(password)){
                        return Result.error(Constants.code_400,"参数错误");
                }
                return Result.success(userService.register(userDTO));
        }

//新增和更新
        @PostMapping
        public Result save(@RequestBody User user){
                return Result.success(userService.saveOrUpdate(user));
        }
        //查询所有数据
        @GetMapping
        public Result findAll(){

                return  Result.success(userService.list());
        }
        //
        @GetMapping("username/{username}")
        public Result findOne(@PathVariable String username){
            QueryWrapper<User> queryWrapper=new QueryWrapper<>();
                queryWrapper.eq("username", username);
                return Result.success(userService.getOne(queryWrapper)) ;
        }
        //删除用户数据
        @DeleteMapping("/{id}")
        public  Result delete(@PathVariable Integer id){

                return Result.success(userService.removeById(id));
        }
        //批量删除
        @PostMapping("/del/batch")
        public  Result deleteBatch(@RequestBody List<Integer> ids){

                return Result.success(userService.removeByIds(ids));
        }
        //分页查询
        // LIMIT第一个参数= (pageNum-1) * pageSize
        //pageSize
        @GetMapping("/page")
        public Result findPage(@RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize,
                                    @RequestParam (defaultValue = "")String username,
                                    @RequestParam (defaultValue = "")String email,
                                    @RequestParam (defaultValue = "")String address){
//       pageNum = (pageNum - 1) *pageSize;
//      List<User> data= userMapper.selectPage(pageNum,pageSize);
//       Integer total=userMapper.selectTotal();
//        Map<String,Object> res= new HashMap<>();
//        res.put("data",data);
//        res.put("total",total);
                IPage<User> page=new Page<>(pageNum,pageSize);
                QueryWrapper<User> queryWrapper =new QueryWrapper<>();
                if(!"".equals(username)){
                        queryWrapper.like("username",username);
                }
                if(!"".equals(email)){
                        queryWrapper.and(w -> w.like("email",email));
                }
                if(!"".equals(address)){
                        queryWrapper.and(w -> w.like("address",address));
                }

//         queryWrapper.like("username",username);
//        queryWrapper.and(w -> w.like("nickname",nickname));
//        queryWrapper.and(w -> w.like("address",address));
                queryWrapper.orderByDesc("id");
                //获取当前用户信息
             User currentUser=   TokenUtils.getCurrentUser();
                System.out.println("获取当前用户信息======================="+currentUser.getNickname());
                return Result.success(userService.page(page,queryWrapper));


        }
        //    导出接口
        @GetMapping("/export")
        public void export(HttpServletResponse response) throws Exception {
                //从数据库查询出所有的数据
                List<User>list=userService.list();
                //通过工具创建writer写出磁盘路径

                //内存操作，写出到浏览器

                ExcelWriter writer = ExcelUtil.getWriter(true);
                //自定义标题别名
                writer.addHeaderAlias("username","用户名");
                writer.addHeaderAlias("password","密码");
                writer.addHeaderAlias("nickname","昵称");
                writer.addHeaderAlias("email","邮箱");
                writer.addHeaderAlias("phone","电话");
                writer.addHeaderAlias("address","地址");
                writer.addHeaderAlias("createTime","创建时间");
                writer.addHeaderAlias("avatarUrl","头像");
                // 一次性写出内容，使用默认样式，强制输出标题
                writer.write(list, true);
                //设置浏览器响应格式
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
                String filename= URLEncoder.encode("用户信息","UTF-8");
                response.setHeader("Content-Disposition","attachment;filename="+filename+".xlsx");
                ServletOutputStream out=response.getOutputStream();
                writer.flush(out,true);
                out.close();
                // 关闭writer，释放内存
                writer.close();
        }
        //导入接口
        @PostMapping("/import")
        public Result imp(MultipartFile file)throws Exception{
                InputStream inputStream=file.getInputStream();
                ExcelReader reader=ExcelUtil.getReader(inputStream);

                //方式一，通过javabean的方式读取excel的对象，但是对象表头必须是英文，跟javabean的属性对应起来
                List<User>list=reader.readAll(User.class);

                userService.saveBatch(list);
                return Result.success(true);
        }

}
