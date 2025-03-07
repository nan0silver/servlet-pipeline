package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.logging.Logger;

public class ChatController extends HttpServlet {
    final static Logger logger = Logger.getLogger(ChatController.class.getName());
    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("서블릿 시작!!!");
    }

//    @Override
//    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
//        super.service(req, res);
//        logger.info("서비스!!!!");
//        res.setContentType("text/plain;charset=UTF-8");
//        res.getWriter().println("안뇽! 받아라!!");
//    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //인코딩
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");


        resp.getWriter().println("Do GET!!!");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //인코딩
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // CORS 헤더 추가
        resp.setHeader("Access-Control-Allow-Origin", "*");  // 모든 origin 허용
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Max-Age", "3600");

        // preflight 요청 처리(OPTIONS 메소드 처리)
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Message message = objectMapper.readValue(req.getInputStream(), Message.class);

        String token = System.getenv("TOGETHER_API_KEY");
        String model = "stabilityai/stable-diffusion-xl-base-1.0";
        String model2 = "black-forest-labs/FLUX.1-schnell-Free";
        Random random = new Random();
        double val = random.nextDouble();
        String prompt = message.content();
        String body = """
                {
                "model": "%s",
                    "prompt": "%s",
                    "width": 1024,
                    "height": 768,
                    "steps": %d,
                    "n": 1
                }
                """.formatted((val > 0.5 ? model : model2), prompt, val > 0.5 ? 40 : 4);
        try {
            Thread.sleep(5000); // 이렇게 된 이상 5초 대기 시킨다 진짜
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.together.xyz/v1/images/generations"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                        .headers(
                                "Authorization", "Bearer %s".formatted(token),
                                "Content-Type", "application/json"
                        ).build();
        String result = "";
        try{
            HttpResponse<String>  response = client.send(request, HttpResponse.BodyHandlers.ofString());
            result = response.body();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        resp.getWriter().println(result);
    }


    @Override
    public void destroy() {
        logger.info("잘있어 서블릿!!!");
        super.destroy();
    }
}

record Message(String content){

}