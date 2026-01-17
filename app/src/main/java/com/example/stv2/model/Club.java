package com.example.stv2.model;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Club {
    private String id;
    private String name;
    private String admin;
    private Book book;
    private Map<String, List<String>> chapters;
    private Map<String, List<String>> customs;
    private Boolean ispublic;
    private List<String> members; //email

    public Club() {}

    public Club(String name, String useremail, Integer chapterdbb, Boolean publice, ArrayList<String> egyedi){
       this.name = name;
       this.admin = useremail;
       this.book = null;
       this.ispublic = publice;
        this.id = UUID.randomUUID().toString();

        this.members = new ArrayList<>();
       this.members.add(useremail);

        this.chapters = new HashMap<>();
        this.customs = new HashMap<>();

        if(chapterdbb<1){
            chapterdbb = 1;
        }

       //chapters ------------------------------------------------------------
        String seged = ". fejezet";
        //ch = 3, akkor 1,2,3
        for (int i = 1; i <= chapterdbb; i++) {
            String szam = Integer.toString(i);
            chapters.put(szam + seged , new ArrayList<>());
        }

        //custom chapters---------------------------------------------------------
        if (egyedi.isEmpty()){
            Log.d("Club", "egyedi empty");
            customs.put("Értékelések", new ArrayList<>());
        } else {
            Log.d("Club", "egyedi van" );
            for (int i = 0; i < egyedi.size(); i++) {
                Log.d("Club", "egyedi: " + egyedi.get(i));
                customs.put(egyedi.get(i), new ArrayList<>());
            }
            customs.put("Értékelések", new ArrayList<>()); //legvégére review chat
        }

    }

    // Getterek Firestore-hoz
    public String getAdmin() {
        return admin;
    }
    public Book getBook() {
        return book;
    }
    public Boolean getIspublic() {
        return ispublic;
    }
    public Map<String, List<String>> getChapters() {
        return chapters;
    }
    public List<String> getMembers() {
        return members;
    }
    public String getName() {
        return name;
    }

    // setterek módosításhoz
    public void setBook(Book book) {
        this.book = book;
    }
    public void setIspublic(Boolean ispublic) {
        this.ispublic = ispublic;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Map<String, List<String>> getCustoms() {
        return customs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public boolean setChapters(int number) {
        String seged = ". fejezet";
        if(number > 0){

            if (chapters == null) {
                chapters = new HashMap<>();
            }

            if(number >= getChaptersSize()){
              //n = 5, get = 2
                // 3 4 5

                //ch = 3, akkor 1,2,3
                for (int i = getChaptersSize()+1; i <= number; i++) {
                    String szam = Integer.toString(i);
                    this.chapters.put(szam + seged , new ArrayList<>());
                }
                return true;
            } else {
                //n = 2, get = 5
                //töröl 5,4,3

                for (int i = getChaptersSize(); i > number; i--) {
                    String szam = Integer.toString(i);
                    chapters.remove(szam + seged);
                }
                return true;
            }
        }
        return false;

    }


    public int getChaptersSize(){
        if (chapters == null) {
            return 0;
        }
        return chapters.size();
    }

    public boolean addMember(String email){
        if (members == null) members = new ArrayList<>();
        if(email != null && !email.isEmpty() && !members.contains(email)  ){
            members.add(email);
            return true ;
        }
        return false;

    }

    public boolean deleteMember(String email){
        if (members == null) members = new ArrayList<>();
        if (email == null) return false;
        if(!email.isEmpty() && members.contains(email)){
            members.remove(email);
            return true ;
        }
        return false;
    }

    public int getMemberSize(){
        if (members == null) members = new ArrayList<String>();
        return members.size();
    }

    public boolean deleteCustom( String name){
        if(customs!= null && name != null && customs.containsKey(name)){
            customs.remove(name);
            return true;
        }
        return false;
    }
    public boolean setCustom(String name){
        if(name!=null && !customs.containsKey(name)){
            customs.put(name, new ArrayList<String>());
            return true;
        } return false;
    }



}
