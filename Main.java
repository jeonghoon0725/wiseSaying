import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Main {

    private static final String directory_name = "db/wiseSaying/";
    private static int id = 0;
    private static List<Map<String, Object>> maps = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("== 명언 앱 ==");

        Scanner in = new Scanner(System.in);

        initBeforeData();//파일 정보 불러오기

        while (true){
            System.out.print("명령) ");
            String line = in.nextLine();

            if(line.equals("종료")) {
                break;
            } else if(line.equals("등록")) {
                createAuthor();
            } else if(line.equals("목록")) {
                searchAuthor();
            } else if(line.indexOf("삭제") == 0) {
                deleteAuthor(line.split("id=")[1]);
            } else if(line.indexOf("수정") == 0) {
                updateAuthor(line.split("id=")[1]);
            } else if(line.equals("빌드")) {
                buildToJson();
            }
        }
    }

    private static void buildToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Map<String, Object> map : maps) {
            JSONObject obj = new JSONObject(map);
            jsonArray.add(obj);
        }
        try {
            FileWriter file2 = new FileWriter(directory_name + "data.json");
            file2.write(String.valueOf(jsonArray));
            file2.flush();
            file2.close();

            System.out.println("data.json 파일의 내용이 갱신되었습니다.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initBeforeData() {
        try {
            //lastId 세팅
            List<String> lines = Files.readAllLines(Paths.get(directory_name+"lastId.txt"));
            id = Integer.parseInt(lines.getFirst());

            //json 파일 데이터 파싱
            File dir = new File(directory_name);
            JSONParser parser = new JSONParser();
            for(String filename : dir.list()) {
                if(!filename.equals("data.json") && filename.endsWith(".json")) {
                    Reader reader = new FileReader(directory_name+filename);
                    JSONObject jsonObject = (JSONObject) parser.parse(reader);

                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("author", jsonObject.get("author"));
                    map.put("content", jsonObject.get("content"));
                    map.put("id", jsonObject.get("id"));

                    maps.add(map);
                }
            }
        } catch (IOException e) {
            id = 0;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateAuthor(String s) {
        int req = Integer.parseInt(s);
        Boolean isException = true;

        for(int i = 0; i < maps.size(); i++) {
            Map<String, Object> map = maps.get(i);
            int res = Integer.parseInt(map.get("id").toString());
            if(res == req) {
                isException = false;

                Scanner in = new Scanner(System.in);

                System.out.println("명언(기존) : " + map.get("content"));
                System.out.print("명언 : ");
                String content = in.nextLine();

                System.out.println("작가(기존) : " + map.get("author"));
                System.out.print("작가 : ");
                String author = in.nextLine();

                map.put("author", author);
                map.put("content", content);

                System.out.println(res+"번 명언이 수정되었습니다.");

                saveFile(map, "U");

                break;
            }
        }

        if(isException) {
            System.out.println(req+"번 명언은 존재하지 않습니다.");
        }
    }

    private static void deleteAuthor(String s) {
        int req = Integer.parseInt(s);
        Boolean isException = true;

        for(int i = 0; i < maps.size(); i++) {
            Map<String, Object> map = maps.get(i);
            int res = Integer.parseInt(map.get("id").toString());
            if(res == req) {
                isException = false;
                maps.remove(i);
                System.out.println(res+"번 명언이 삭제되었습니다.");
                saveFile(map, "D");
                break;
            }
        }

        if(isException) {
            System.out.println(req+"번 명언은 존재하지 않습니다.");
        }
    }

    private static void searchAuthor() {
        System.out.println("번호 / 작가 / 명언");
        System.out.println("---------------------- id : " + id);

        //id / author / content
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < maps.size(); i++) {
            Map<String, Object> map = maps.get(i);
            if(i > 0) {
                sb.insert(0, "\r\n");
            }
            sb.insert(0, map.get("id") + " / " + map.get("author") + " / " + map.get("content"));
        }
        System.out.println(sb.toString());
    }

    private static void createAuthor() {
        Scanner in = new Scanner(System.in);

        System.out.print("명언 : ");
        String content = in.nextLine();

        System.out.print("작가 : ");
        String author = in.nextLine();

        id++;
        System.out.println(id+"번 명언이 등록되었습니다.");

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("author", author);
        map.put("content", content);
        maps.add(map);

        saveFile(map, "C");
    }

    private static void saveFile(Map<String, Object> map, String type) {
        if(type.equals("D")) {//이용중인 파일은 삭제 불가능
            /*try {
                Path file_name = Paths.get(directory_name + map.get("id") + ".json");
                Files.delete(file_name);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        } else {
            JSONObject obj = new JSONObject();
            String key = "";
            Object value = null;
            for(Map.Entry<String, Object> entry : map.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();
                obj.put(key, value);
            }

            try {
                FileWriter file1 = new FileWriter(directory_name + map.get("id") + ".json");
                file1.write(obj.toString());
                file1.flush();
                file1.close();

                if(type.equals("C")) {
                    FileWriter file2 = new FileWriter(directory_name + "lastId.txt");
                    file2.write(map.get("id").toString());
                    file2.flush();
                    file2.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
