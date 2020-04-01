import groovy.sql.Sql
import io.confluent.demo.Movie
import io.confluent.demo.Parser

class SqlLoader {

  static void main(String[] args) {

    Properties props = new Properties()
    props.load(new FileInputStream(new File(args[0])))

    def moviesFile = new File(props.get('movies.file') as String)
    println "Movies File at ${moviesFile.absolutePath}"

    def inserMoviePs = """INSERT INTO movies (movie_id, title, release_year, country, genres, actors, directors, composers, screenwriters, cinematographer, production_companies) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"""
    try {
      def dbURL = 'jdbc:mysql://localhost:3306/demo'
      def dbUserName = 'root'
      def dbPassword = 'debezium'
      def dbDriver = 'com.mysql.jdbc.Driver'
      def db = Sql.newInstance(dbURL, dbUserName, dbPassword, dbDriver)
      moviesFile.eachLine { line ->
        db.withBatch(100, inserMoviePs) { ps ->
          Movie movie = Parser.parseMovie(line)
          ps.addBatch(movie.movieId,
                      movie.title,
                      movie.releaseYear,
                      movie.country,
                      movie.genres.join('|'),
                      movie.actors.join('|'),
                      movie.directors.join('|'),
                      movie.composers.join('|'),
                      movie.screenwriters.join('|'),
                      movie.cinematographer,
                      movie.productionCompanies.join('|')
          )
        }
      }
      println 'Good'
    }

    catch (
        Exception e
        ) {
      println 'DB Error'
      println e.getMessage()
    } finally {

    }
  }

}