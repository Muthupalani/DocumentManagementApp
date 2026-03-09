package com.documentapp.controller;

import com.documentapp.dao.FileDAO;
import com.documentapp.dao.FileVersionDAO;
import com.documentapp.dao.UserDAO;
import com.documentapp.model.Files;
import com.documentapp.model.FileVersion;
import com.documentapp.model.User;
import com.documentapp.util.DBConnection;
import com.documentapp.util.RedisUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;

public class VersionDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Long userIds = (Long) request.getAttribute("userId");

        if (userIds == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        long userId = userIds;

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserById(con, userId);

            if (user == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            long versionId;

            try {
                versionId = Long.parseLong(request.getParameter("versionId"));
            } catch (Exception e) {
                response.sendRedirect("files");
                return;
            }

            FileVersion version = null;

            String redisKey = "version:" + versionId;

            try (Jedis jedis = RedisUtil.getConnection()) {

                if (jedis.exists(redisKey)) {

                    String data = jedis.get(redisKey);
                    String[] p = data.split("\\&", -1);

                    version = new FileVersion();

                    version.setId(Long.parseLong(p[0]));
                    version.setFileId(Long.parseLong(p[1]));
                    version.setVersionNumber(Integer.parseInt(p[2]));
                    version.setUploadedTime(
                            p[3].equals("null") ? null : Timestamp.valueOf(p[3])
                    );
                    version.setFileSize(Long.parseLong(p[4]));
                    version.setFileType(p[5].equals("null") ? null : p[5]);
                    version.setFilePath(p[6]);
                    version.setNotes(p[7].equals("null") ? null : p[7]);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (version == null) {

                FileVersionDAO versionDAO = new FileVersionDAO();

                version = versionDAO.getVersionById(con, versionId);

                if (version == null) {
                    response.sendRedirect("files");
                    return;
                }

                try (Jedis jedis = RedisUtil.getConnection()) {

                    String value =
                            version.getId() + "&" +
                            version.getFileId() + "&" +
                            version.getVersionNumber() + "&" +
                            version.getUploadedTime() + "&" +
                            version.getFileSize() + "&" +
                            version.getFileType() + "&" +
                            version.getFilePath() + "&" +
                            version.getNotes();

                    jedis.setex(redisKey, 180, value);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            FileDAO fileDAO = new FileDAO();

            Files file = fileDAO.getFileById(con, version.getFileId());

            if (file == null || file.getUploadedBy() != user.getId()) {
                response.sendRedirect("files");
                return;
            }

            request.setAttribute("version", version);
            request.setAttribute("file", file);

            request.getRequestDispatcher("versionDetails.jsp")
                    .forward(request, response);

        } catch (Exception e) {

            e.printStackTrace();
            response.sendRedirect("error.jsp");

        } finally {

            DBConnection.close(con);
        }
    }
}