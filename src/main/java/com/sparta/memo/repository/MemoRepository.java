package com.sparta.memo.repository;


import com.sparta.memo.dto.MemoRequestDto;
import com.sparta.memo.dto.MemoResponseDto;
import com.sparta.memo.entity.Memo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MemoRepository {
  private final JdbcTemplate jdbcTemplate;

  public MemoRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Memo save(Memo memo) {
    KeyHolder keyHolder = new GeneratedKeyHolder(); // 기본 키를 반환받기 위한 객체

    String sql = "INSERT INTO memo (username, contents) VALUES (?, ?)";
    // INSERT, DELETE, UPDATE 모두 jdbcTemplate.update()를 사용
    jdbcTemplate.update(con -> {
              PreparedStatement preparedStatement = con.prepareStatement(sql,
                      Statement.RETURN_GENERATED_KEYS);
              // VALUES (?, ?)에 1번째 ?, 2번째 ?에 들어가는 값
              preparedStatement.setString(1, memo.getUsername());
              preparedStatement.setString(2, memo.getContents());
              return preparedStatement;
            },
            keyHolder);

    // DB Insert 후 받아온 기본키 확인
    Long id = keyHolder.getKey().longValue();
    memo.setId(id);
    return memo;
  }


  public List<MemoResponseDto> findAll() {
    String sql = "SELECT * FROM memo";

    // SELECT는 jdbcTemplate.query()사용
    return jdbcTemplate.query(sql, new RowMapper<MemoResponseDto>() {
      @Override
      public MemoResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        // SQL 의 결과로 받아온 Memo 데이터들을 MemoResponseDto 타입으로 변환해줄 메서드
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String contents = rs.getString("contents");
        return new MemoResponseDto(id, username, contents);
      }
    });
  }

  public void update(Long id, MemoRequestDto requestDto) {
    String sql = "UPDATE memo SET username = ?, contents = ? WHERE id = ?";
    jdbcTemplate.update(sql, requestDto.getUsername(), requestDto.getContents(), id);
  }

  public void delete(Long id) {
    // memo 삭제
    String sql = "DELETE FROM memo WHERE id = ?";
    jdbcTemplate.update(sql, id);
  }


  public Memo findById(Long id) {
    // DB 조회
    String sql = "SELECT * FROM memo WHERE id = ?";

    return jdbcTemplate.query(sql, resultSet -> {
      if (resultSet.next()) {
        Memo memo = new Memo();
        memo.setUsername(resultSet.getString("username"));
        memo.setContents(resultSet.getString("contents"));
        return memo;
      } else {
        return null;
      }
    }, id);
  }

}