package github.yix.eurekaclient.controller;

import github.yix.eurekaclient.domain.Result;
import github.yix.eurekaclient.domain.User;
import github.yix.eurekaclient.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {
    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public Result create(@RequestBody User user) {
        userService.create(user);
        return new Result("操作成功", 200);
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        LOGGER.info("根据id获取用户信息，用户名称为：{}",user.getUsername());
        return new Result<>(user);
    }

    @GetMapping("/getUserByIds")
    public Result<List<User>> getUserByIds(@RequestParam List<Long> ids) {
        List<User> userList= userService.getUserByIds(ids);
        LOGGER.info("根据ids获取用户信息，用户列表为：{}",userList);
        return new Result<>(userList);
    }

    @GetMapping("/getByUsername")
    public Result<User> getByUsername(@RequestParam String username) {
        User user = userService.getByUsername(username);
        return new Result<>(user);
    }

    @PostMapping("/update")
    public Result update(@RequestBody User user) {
        userService.update(user);
        return new Result("操作成功", 200);
    }

    @PostMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        userService.delete(id);
        return new Result("操作成功", 200);
    }

}
