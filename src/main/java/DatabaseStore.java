import java.io.*;
import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Relational MySQL adapter retaining the application's existing stream interface. */
public final class DatabaseStore {
 private DatabaseStore() {}
 private static final String[] TYPES={"profile_","curriculum_","schedule_","grades_"};
 private static String key(File f){return f.getName();}
 
 private static Properties loadDatabaseSettings() {
  Properties settings = new Properties();
  java.nio.file.Path file = java.nio.file.Paths.get("config", "database.properties");
  if (java.nio.file.Files.isRegularFile(file)) {
   try (InputStream in = java.nio.file.Files.newInputStream(file)) {
    settings.load(in);
   } catch (IOException e) {
    throw new IllegalStateException("Unable to read " + file.toAbsolutePath(), e);
   }
  }
  return settings;
 }
 private static String setting(String envName, String propertyName, String defaultValue) {
  String override = System.getenv(envName);
  if (override != null && !override.isBlank()) return override;
  return loadDatabaseSettings().getProperty(propertyName, defaultValue);
 }
 private static Connection connect() throws SQLException {
  String url = setting("ACADEMIA_DB_URL", "db.url", "jdbc:mysql://localhost:3306/academia_one_fresh?serverTimezone=UTC");
  String user = setting("ACADEMIA_DB_USER", "db.user", "root");
  String password = setting("ACADEMIA_DB_PASSWORD", "db.password", "");
  if (password.equals("CHANGE_ME") || password.equals("YOUR_LOCAL_MYSQL_PASSWORD")) {
   throw new SQLException("MySQL password is not configured. Open config/database.properties and replace CHANGE_ME with the password for this computer.");
  }
  return DriverManager.getConnection(url, user, password);
 }
 private static String type(String key){if(key.equals("users.txt"))return "users";for(String t:TYPES)if(key.startsWith(t)&&key.endsWith(".txt"))return t;return "archive";}
 private static String suffix(String k){return k.substring(k.indexOf('_')+1,k.length()-4);}
 private static String sha(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder x=new StringBuilder();for(byte z:b)x.append(String.format("%02x",z&255));return x.toString().substring(0,12);}catch(Exception e){throw new IllegalStateException(e);}}
 private static long student(Connection c,String k)throws SQLException{
  try(PreparedStatement p=c.prepareStatement("SELECT id,username FROM students");ResultSet r=p.executeQuery()){while(r.next())if(sha(r.getString(2)).equals(suffix(k)))return r.getLong(1);}return -1;
 }
 private static long subject(Connection c,String code,String title,int units)throws SQLException{
  if(code.isBlank())code="UNSPECIFIED";
  try(PreparedStatement p=c.prepareStatement("INSERT INTO subjects(code,title,default_units) VALUES(?,?,?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)")){p.setString(1,code);p.setString(2,title.isBlank()?code:title);p.setInt(3,Math.max(0,units));p.executeUpdate();try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT LAST_INSERT_ID()")){r.next();return r.getLong(1);}}
 }
 private static String[] split(String s){return s.split("\\|",-1);}
 private static int num(String s,int d){try{return Integer.parseInt(s.trim());}catch(Exception e){return d;}}
 private static String val(String[] a,int i){return i<a.length?a[i]:"";}
 private static String read(Connection c,String k)throws SQLException{
  String t=type(k);StringBuilder b=new StringBuilder();
  if(t.equals("archive")){try(PreparedStatement p=c.prepareStatement("SELECT content FROM legacy_archive WHERE document_key=?")){p.setString(1,k);try(ResultSet r=p.executeQuery()){return r.next()?new String(r.getBytes(1),StandardCharsets.UTF_8):null;}}}
  if(t.equals("users")){try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT username,password_hash FROM students ORDER BY id")){while(r.next())b.append(r.getString(1)).append('|').append(r.getString(2)).append('\n');}return b.toString();}
  long id=student(c,k);if(id<0)return null;
  if(t.equals("profile_")){String[] cols={"fullName","studentId","program","yearLevel","section","email","mobile","address","gender","profilePicture","major"};try(PreparedStatement p=c.prepareStatement("SELECT full_name,school_student_id,program,year_level,section,email,mobile,address,gender,profile_picture,major FROM students WHERE id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){if(r.next())for(int i=0;i<cols.length;i++)b.append(cols[i]).append('|').append(Objects.toString(r.getString(i+1),"")).append('\n');}}}
  else if(t.equals("curriculum_")){try(PreparedStatement p=c.prepareStatement("SELECT ce.year_level,ce.semester,s.code,s.title,ce.units FROM curriculum_entries ce JOIN subjects s ON s.id=ce.subject_id WHERE ce.student_id=? ORDER BY ce.id")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())for(int i=1;i<=5;i++){b.append(Objects.toString(r.getString(i),""));b.append(i==5?'\n':'|');}}}}
  else if(t.equals("grades_")){try(PreparedStatement p=c.prepareStatement("SELECT s.code,g.grade_value FROM student_grades g JOIN subjects s ON s.id=g.subject_id WHERE g.student_id=? ORDER BY g.id")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())b.append(r.getString(1)).append('|').append(r.getString(2)).append('\n');}}}
  else if(t.equals("schedule_")){try(PreparedStatement p=c.prepareStatement("SELECT year_level,semester,day_expression,TIME_FORMAT(start_time,'%h:%i %p'),TIME_FORMAT(end_time,'%h:%i %p'),original_subject,room,class_type,instructor,units FROM class_schedules WHERE student_id=? ORDER BY id")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next()){for(int i=1;i<=10;i++)b.append(Objects.toString(r.getString(i),"")).append(i==10?'\n':'|');}}}}
  return b.toString();
 }
 private static void write(Connection c,String k,String value)throws SQLException{
  String t=type(k);
  if(t.equals("archive")){try(PreparedStatement p=c.prepareStatement("INSERT INTO legacy_archive(document_key,content) VALUES(?,?) ON DUPLICATE KEY UPDATE content=VALUES(content)")){p.setString(1,k);p.setBytes(2,value.getBytes(StandardCharsets.UTF_8));p.executeUpdate();}return;}
  if(t.equals("users")){for(String line:value.split("\\R")){String[] a=split(line);if(a.length<2||a[0].isBlank())continue;try(PreparedStatement p=c.prepareStatement("INSERT INTO students(username,password_hash) VALUES(?,?) ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash)")){p.setString(1,a[0]);p.setString(2,a[1]);p.executeUpdate();}}return;}
  long id=student(c,k);if(id<0)throw new SQLException("Student not found for "+k+"; create the account first");
  if(t.equals("profile_")){Map<String,String> m=new HashMap<>();for(String line:value.split("\\R")){String[] a=line.split("\\|",2);if(a.length==2)m.put(a[0],a[1]);}String[] keys={"fullName","studentId","program","yearLevel","section","email","mobile","address","gender","profilePicture","major"};try(PreparedStatement p=c.prepareStatement("UPDATE students SET full_name=?,school_student_id=?,program=?,year_level=?,section=?,email=?,mobile=?,address=?,gender=?,profile_picture=?,major=? WHERE id=?")){for(int i=0;i<keys.length;i++)p.setString(i+1,m.getOrDefault(keys[i],""));p.setLong(12,id);p.executeUpdate();}return;}
  String table=t.equals("curriculum_")?"curriculum_entries":t.equals("schedule_")?"class_schedules":"student_grades";
  try(PreparedStatement p=c.prepareStatement("DELETE FROM "+table+" WHERE student_id=?")){p.setLong(1,id);p.executeUpdate();}
  for(String line:value.split("\\R")){if(line.isBlank())continue;String[] a=split(line);
   if(t.equals("curriculum_")){if(a.length<5)continue;long sid=subject(c,a[2],a[3],num(a[4],3));try(PreparedStatement p=c.prepareStatement("INSERT INTO curriculum_entries(student_id,subject_id,year_level,semester,units) VALUES(?,?,?,?,?)")){p.setLong(1,id);p.setLong(2,sid);p.setInt(3,num(a[0],1));p.setInt(4,num(a[1],1));p.setInt(5,num(a[4],3));p.executeUpdate();}}
   else if(t.equals("grades_")){if(a.length<2)continue;long sid=subject(c,a[0],a[0],3);try(PreparedStatement p=c.prepareStatement("INSERT INTO student_grades(student_id,subject_id,grade_value) VALUES(?,?,?)")){p.setLong(1,id);p.setLong(2,sid);p.setString(3,a[1]);p.executeUpdate();}}
   else if(t.equals("schedule_")){
    int shift=a.length>=9?2:0;if(a.length<6)continue;String subjectText=val(a,shift+3);String code=subjectText.contains(" - ")?subjectText.substring(0,subjectText.indexOf(" - ")).trim():subjectText.trim();int units=num(val(a,9),3);long sid=subject(c,code,subjectText,units);
    try(PreparedStatement p=c.prepareStatement("INSERT INTO class_schedules(student_id,subject_id,original_subject,year_level,semester,day_expression,start_time,end_time,room,class_type,instructor,units) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)")){
     p.setLong(1,id);p.setLong(2,sid);p.setString(3,subjectText);p.setInt(4,shift==2?num(a[0],1):1);p.setInt(5,shift==2?num(a[1],1):1);p.setString(6,val(a,shift));p.setTime(7,parseTime(val(a,shift+1)));p.setTime(8,parseTime(val(a,shift+2)));p.setString(9,val(a,shift+4));p.setString(10,val(a,shift+5));p.setString(11,val(a,shift+6));p.setInt(12,units);p.executeUpdate();}
   }
  }
 }
 private static Time parseTime(String s)throws SQLException{try{return Time.valueOf(LocalTime.parse(s.trim().toUpperCase(Locale.ROOT),DateTimeFormatter.ofPattern("h:mm a",Locale.US)));}catch(Exception e){try{return Time.valueOf(s);}catch(Exception ignored){throw new SQLException("Invalid schedule time: "+s,e);}}}
 public static boolean emailOrStudentIdExists(String email,String studentId){
  try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT 1 FROM students WHERE LOWER(email)=LOWER(?) OR school_student_id=? OR LOWER(username)=LOWER(?) LIMIT 1")){
   p.setString(1,email);p.setString(2,studentId);p.setString(3,email);
   try(ResultSet r=p.executeQuery()){return r.next();}
  }catch(SQLException e){throw new IllegalStateException("Could not validate registration: "+e.getMessage(),e);}
 }
 public static boolean exists(File f){try(Connection c=connect()){String k=key(f);if(type(k).equals("archive")){try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM legacy_archive WHERE document_key=?")){p.setString(1,k);try(ResultSet r=p.executeQuery()){return r.next();}}}if(type(k).equals("users")){try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT 1 FROM students LIMIT 1")){return r.next();}}return student(c,k)>0;}catch(SQLException e){throw new IllegalStateException("MySQL unavailable: "+e.getMessage(),e);}}
 public static InputStream input(File f)throws IOException{try(Connection c=connect()){String v=read(c,key(f));if(v==null)throw new FileNotFoundException(key(f));return new ByteArrayInputStream(v.getBytes(StandardCharsets.UTF_8));}catch(SQLException e){throw new IOException("MySQL read failed: "+e.getMessage(),e);}}
 public static OutputStream output(File f,boolean append)throws IOException{String k=key(f);ByteArrayOutputStream b=new ByteArrayOutputStream();if(append&&exists(f))try(InputStream in=input(f)){in.transferTo(b);}return new FilterOutputStream(b){boolean closed;public void close()throws IOException{if(closed)return;closed=true;super.close();try(Connection c=connect()){c.setAutoCommit(false);try{DatabaseStore.write(c,k,b.toString(StandardCharsets.UTF_8));c.commit();}catch(Exception e){c.rollback();throw e;}}catch(Exception e){throw new IOException("MySQL relational write failed: "+e.getMessage(),e);}}};}
 public static boolean delete(File f){try(Connection c=connect()){String k=key(f);if(type(k).equals("archive")){try(PreparedStatement p=c.prepareStatement("DELETE FROM legacy_archive WHERE document_key=?")){p.setString(1,k);return p.executeUpdate()>0;}}return false;}catch(SQLException e){throw new IllegalStateException(e);}}
 public static boolean replace(File from,File to){try(InputStream in=input(from);OutputStream out=output(to,false)){in.transferTo(out);}catch(IOException e){return false;}return delete(from);}
}
