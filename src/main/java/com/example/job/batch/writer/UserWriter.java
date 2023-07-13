package com.example.job.batch.writer;


import com.example.job.batch.model.User;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserWriter implements ItemWriter<User> {

    private final JdbcTemplate batchTemplate;

    @Override
    public void write(List<? extends User> list) throws Exception {
        System.out.println("write===========");
        String sql = "insert into admin_account1(created_at, updated_at,  password, phone, username, valid_status) values(?,?,?,?,?,?)";
        for (User user : list) {
            Object[] args = {new Date().getTime(), new Date().getTime(), user.getPassword(), user.getPhone(), user.getUsername(), user.getValidStatus()};
            int insert = batchTemplate.update(sql, args);
            System.out.println(insert);
        }
    }

}
