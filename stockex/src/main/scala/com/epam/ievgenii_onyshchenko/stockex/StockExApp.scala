package com.epam.ievgenii_onyshchenko.stockex

import scala.io.StdIn
import cats.data.{IndexedStateT, State}
import State.{get, modify}
import cats.Eval

import scala.annotation.tailrec
import scala.collection.immutable
import scala.util.matching.Regex

object StockExApp extends App {
  val sellPattern:Regex = "^SELL\\s+(\\d+)\\s+(\\d+)\\s*$".r
  val buyPattern:Regex = "^BUY\\s+(\\d+)\\s+(\\d+)\\s*$".r
  val listPattern:Regex = "^LIST\\s*$".r

  val commandState: State[Database, String] = for {
    _ <- get
    command <- State.pure(parseCommand(StdIn.readLine))
    _ <- State.modify(command.modify)
    prompt <- State.apply[Database, String](output(_, command))
  } yield prompt


  println("Enter command")
  run(new Database())

  @tailrec
  def run(database: Database): Unit ={
    val exec = commandState.run(database).value
    println(exec._2)
    run(exec._1)
  }

  def parseCommand(input:String): Command ={
    input match {
      case sellPattern(price, amount) => SellCommand(price.toInt, amount.toInt)
      case buyPattern(price, amount) => BuyCommand(price.toInt, amount.toInt)
      case listPattern() => ListCommand
      case _ => IllegalCommand
    }
  }

  def output (database: Database, command: Command):(Database, String) ={
    database->command.prompt(database)
  }

  sealed trait Command {
    def prompt(database: Database):String


    def modify(database:Database):Database
  }

  case class SellCommand(price: Int, amount: Int) extends Command{
    override def modify(database: Database): Database = database.addBid(this)

    override def prompt(database: Database): String = "Accepted"
  }

  case class BuyCommand(price: Int, amount: Int) extends Command  {
    override def modify(database: Database): Database = database.addBid(this)

    override def prompt(database: Database): String = "Accepted"
  }

  case object ListCommand extends Command{
    override def modify(database: Database): Database = database

    override def prompt(database: Database): String = {
      val buys = for {
        (price, item)<-database.bids
        if item.buy>item.sell
      }yield s"BUY ${price} ${item.buy-item.sell}"

      val sells = for {
        (price, item)<-database.bids
        if item.sell>item.buy
      }yield s"BUY ${price} ${item.sell-item.buy}"

      val trades = for {
        (price, item)<-database.bids
        if item.buy!=0 && item.sell!=0
      }yield s"TRADE ${price} ${Math.max(item.buy,item.sell) - Math.abs(item.buy-item.sell)}"


      buys.mkString("Buys\n", "\n", "\n")+sells.mkString("Sells\n", "\n", "\n")+trades.mkString("Trades:\n", "\n", "")
    }
  }

  case object IllegalCommand extends Command{
    override def modify(database: Database): Database = database

    override def prompt(database: Database): String = "Illegal command"
  }

  case class Item(buy:Int=0, sell:Int=0){
    def addBuy(amount: Int): Item ={
      this.copy(buy=this.buy+amount)
    }
    def addSell(amount: Int): Item ={
      this.copy(sell=this.sell+amount)
    }
  }
  case class Database(bids:Map[Int, Item]=Map.empty) {

    def addBid(command: Command): Database ={
      val s: (Int, Item) =command match {
        case b: BuyCommand => b.price->this.bids.getOrElse(b.price, Item()).addBuy(b.amount)
        case s: SellCommand => s.price->this.bids.getOrElse(s.price, Item()).addSell(s.amount)
      }
      this.copy(this.bids + s)
    }
  }
}
