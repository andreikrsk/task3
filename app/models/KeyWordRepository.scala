package models
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ Future, ExecutionContext }
/**
  * A repository for people.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */


@Singleton
class KeyWordRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._
  /**
    * Here we define the table. It will have a name of people
    */
  class KeyWordTable(tag: Tag) extends Table[KeyWord](tag, "key_word") {
    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    /** The name column */
    def word = column[String]("word")
    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Person object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Person case classes
      * apply and unapply methods.
      */
    def * = (id, word) <> ((KeyWord.apply _).tupled, KeyWord.unapply)
  }
  /**
    * The starting point for all queries on the people table.
    */
  val keyWord = TableQuery[KeyWordTable]
  /**
    * Create a person with the given name and age.
    *
    * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
    * id for that person.
    */
  def create(word: String): Future[KeyWord] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (keyWord.map(k => (k.word))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning keyWord.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((word, id) => KeyWord(id, word))
      // And finally, insert the person into the database
      ) += (word)
  }
  /**
    * List all the people in the database.
    */
  def list(): Future[Seq[KeyWord]] = db.run {
    keyWord.result
  }

  def del(del_id: Int ) = db.run {
    println("inside of del method KeyWord")
    keyWord.filter(_.id === del_id).delete
  }
}