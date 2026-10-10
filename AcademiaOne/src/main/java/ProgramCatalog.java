import javax.swing.*;
import java.util.*;

/** Static program catalog; examples are configurable and not an official university catalog. */
public final class ProgramCatalog {
    private ProgramCatalog() {}
    private static final LinkedHashMap<String,String[]> OPTIONS=new LinkedHashMap<>();
    static {
        add("BS Computer Science (BSCS)","Not applicable","Artificial Intelligence","Data Science","Software Engineering","Cybersecurity");
        add("BS Information Systems (BSIS)","Not applicable","Business Analytics","Enterprise Systems","Information Systems Management");
        add("BS Information Technology (BSIT)","Not applicable","Web and Mobile Development","Network Administration","Cybersecurity","Cloud Computing");
        add("BS Computer Engineering", "Not applicable", "Embedded Systems", "Computer Networks", "Robotics");
        add("BS Criminology","Not applicable","Forensic Science","Crime Investigation");
        add("BS Tourism Management","Not applicable","Travel Operations","Events Management","Hospitality Tourism");
        add("BS Hospitality Management","Not applicable","Culinary Arts","Hotel Operations","Food and Beverage Management");
        add("BS Business Administration","Financial Management","Marketing Management","Human Resource Management","Operations Management");
        add("BS Accountancy","Not applicable");
        add("BS Entrepreneurship","Not applicable");
        add("BS Development Communication","Not applicable","Community Communication","Development Journalism");
        add("Bachelor of Performing Arts","Dance","Theater");
        add("Bachelor of Early Childhood Education","Not applicable");
        add("Bachelor of Special Needs Education","Not applicable");
        add("Bachelor of Physical Education","Not applicable");
        add("Bachelor of Culture and Arts Education","Not applicable");
        add("Bachelor of Secondary Education","English","Mathematics","Science","Filipino","Social Studies","Values Education");
        add("Bachelor of Elementary Education","General Education");
        add("BS Psychology","Not applicable","Industrial Psychology");
        add("BS Nursing","Not applicable");
        add("BS Biology","Not applicable","Microbiology","Ecology");
        add("BS Civil Engineering","Not applicable","Structural Engineering","Construction Management");
        add("BS Electrical Engineering","Not applicable","Power Systems","Electronics");
        add("BS Mechanical Engineering","Not applicable","Manufacturing","Energy Systems");
        add("Associate in Computer Technology (ACT)","Application Development","Multimedia","Networking");
    }
    private static void add(String course,String... majors){OPTIONS.put(course,majors);}
    public static JComboBox<String> courseCombo(){return new JComboBox<>(OPTIONS.keySet().toArray(new String[0]));}
    public static void selectCourse(JComboBox<String> course,String existing){
        if(existing!=null&&!existing.isBlank()){
            boolean found=false;
            for(int i=0;i<course.getItemCount();i++)if(existing.equalsIgnoreCase(course.getItemAt(i))){found=true;break;}
            if(!found)course.addItem(existing);
            course.setSelectedItem(existing);
        }
    }
    public static void bind(JComboBox<String> course,JComboBox<String> major,String existing){
        Runnable refresh=()->{
            String selected=(String)course.getSelectedItem();
            major.removeAllItems();
            String[] majors=OPTIONS.getOrDefault(selected,new String[]{"Not applicable"});
            for(String option:majors)major.addItem(option);
            if(existing!=null&&!existing.isBlank()){
                boolean found=false;
                for(String option:majors)if(option.equalsIgnoreCase(existing))found=true;
                if(found)major.setSelectedItem(existing);
            }
            major.setEnabled(majors.length>1||!"Not applicable".equals(majors[0]));
        };
        course.addActionListener(e->refresh.run());refresh.run();
    }
    public static String selectedMajor(JComboBox<String> combo){
        String result=String.valueOf(combo.getSelectedItem());return "Not applicable".equals(result)||"null".equals(result)?"":result;
    }
}
