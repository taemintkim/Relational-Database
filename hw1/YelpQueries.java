import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class YelpQueries
{
  public static void main(String[] args) throws ClassNotFoundException
  {
    // load the sqlite-JDBC driver using the current class loader
    Class.forName("org.sqlite.JDBC");

    String dbLocation = "yelp_dataset.db"; 

    Connection connection = null;
    try
    {
      // create a database connection
      connection = DriverManager.getConnection("jdbc:sqlite:" + dbLocation);

      Statement statement = connection.createStatement();

      // Question 0
      statement.execute("DROP VIEW IF EXISTS q0"); // Clean out views
      String q0 = "CREATE VIEW q0 AS "
                   + "SELECT count(*) FROM reviews";
      statement.execute(q0);

      // Question 1
      statement.execute("DROP VIEW IF EXISTS q1");
      String q1 = "CREATE VIEW q1 AS " 
                  + "SELECT avg(review_count) from users where review_count < 10"; // Replace this line
      statement.execute(q1);

      // Question 2
      statement.execute("DROP VIEW IF EXISTS q2");
      String q2 = "CREATE VIEW q2 AS "
                  + "SELECT name from users where yelping_since > '2014-11' and review_count > 50"; // Replace this line
      statement.execute(q2);

      // Question 3
      statement.execute("DROP VIEW IF EXISTS q3");
      String q3 = "CREATE VIEW q3 AS "
                  + "SELECT name, stars from businesses where stars > 3 and city = 'Pittsburgh'"; // Replace this line
      statement.execute(q3);

      // Question 4
      statement.execute("DROP VIEW IF EXISTS q4");
      String q4 = "CREATE VIEW q4 AS "
                  + "select name from businesses where review_count > 500 order by stars limit 1"; // Replace this line
      statement.execute(q4);

      // Question 5
      statement.execute("DROP VIEW IF EXISTS q5");
      String q5 = "CREATE VIEW q5 AS "
                  + "SELECT B.name from checkins as A, businesses as B where A.business_id = B.business_id and A.day = 0 order by num_checkins desc limit 5"; // Replace this line
      statement.execute(q5);

      // Question 6
      statement.execute("DROP VIEW IF EXISTS q6");
      String q6 = "CREATE VIEW q6 AS "
                  + "select day from checkins group by day order by sum(num_checkins) desc limit 1"; // Replace this line
      statement.execute(q6);

      // Question 7
      statement.execute("DROP VIEW IF EXISTS q7");
      String q7 = "CREATE VIEW q7 AS "
                  + "SELECT B.name from businesses as B, users as A, reviews as C where A.user_id = C.user_id and B.business_id = C.business_id and A.review_count = (select max(review_count) from users)"; // Replace this line
      statement.execute(q7);

      // Question 8
      // with sub(counttt) as (select count(*) from businesses where city = 'Edinburgh') 
      statement.execute("DROP VIEW IF EXISTS q8");
      String q8 = "CREATE VIEW q8 AS "
                  + "Select avg(stars) from (SELECT stars from businesses where city = 'Edinburgh' order by review_count desc limit (select count(*)/10 from businesses where city = 'Edinburgh'))"; // Replace this line
      statement.execute(q8);

      // Question 9
      statement.execute("DROP VIEW IF EXISTS q9");
      String q9 = "CREATE VIEW q9 AS "
                  + "SELECT name from users where name like '%..%'"; // Replace this line
      statement.execute(q9);

      // Question 10
      statement.execute("DROP VIEW IF EXISTS q10");
      String q10 = "CREATE VIEW q10 AS "
                  + "SELECT city from (select B.city, count(B.business_id) as BusPerCity from businesses as B, users as U, reviews as R where U.user_id = R.user_id and R.business_id = B.business_id and R.user_id in (SELECT user_id from users where name like '%..%') group by B.city) order by BusPerCity desc limit 1"; // Replace this line
      statement.execute(q10);

      connection.close();

    }
    catch(SQLException e)
    {
      // if the error message is "out of memory", 
      // it probably means no database file is found
      System.err.println(e.getMessage());
    }
    finally
    {
      try
      {
        if(connection != null)
          connection.close();
      }
      catch(SQLException e)
      {
        // connection close failed.
        System.err.println(e);
      }
    }
  }
}
