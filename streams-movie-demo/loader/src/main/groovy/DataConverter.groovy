package io.confluent.demo;

import java.io.File;

File ratings = new File('../../../data/ratings.dat')
new File('../../../data/ratings-json.js').withPrintWriter { pw ->
    ratings.eachLine { line ->
        Rating rating = Parser.parseRating(line)
        def json = Parser.toJson(rating)
        pw.println(json.toString())
    }
}


File movies = new File('../../../data/movies.dat')
new File('../../../data/movies-json.js').withPrintWriter { pw ->
    movies.eachLine { line ->
        pw.println(Parser.toJson(Parser.parseMovie(line)).toString())
    }
}