
package com.fishuyo.seer
package spacetree2

import spatial._
import graphics._
import actor._

import akka.actor._
import akka.event.Logging

import collection.mutable.ListBuffer
import collection.mutable.HashMap
import collection.mutable.HashSet


class Branch(var parent:Branch, var pos:Vec3, var growDirection:Vec3){
  var growDirection0 = Vec3(growDirection)
  var growCount = 0
  var age = 0

  val children = ListBuffer[Branch]()

  def grow(){
    age += 1
    if(parent != null) parent.grow()
  }

  def reset(){
    growCount = 0
    age = 0
    growDirection = growDirection0
  }
}

class Leaf(var pos:Vec3){
  var closest:Branch = null
}

class AntiLeaf(var pos:Vec3, var vel:Vec3)




class Tree {

  var minDistance = 0.05
  var maxDistance = 0.1 //0.35
  var branchLength = 0.04

  var trimDistance = maxDistance * 4
  var thresholdVel = 0.09f
  var thresholdVelMax = 0.2f

  var root = new Branch(null, Vec3(), Vec3(0,1,0))

  var leaves = ListBuffer[Leaf]()
  var antiLeaves = ListBuffer[AntiLeaf]()

  val currentViewBranches = ListBuffer[Branch]() // branches to render
  val growthHistory = ListBuffer[List[Branch]]() // branches added at each growth step

  val branches = Octree[Branch](Vec3(0),5) // current simulation set of branches

  var growIteration = 0
  var viewIteration = 0
  var dirty = false
  var sleep = 300

  branches += (root.pos -> root)

  val actor = System().actorOf( Props(new TreeGrowActor(this)))

  actor ! "start"

  def reset(){
    branches.clear
    currentViewBranches.clear
    growthHistory.clear
    leaves.clear
    antiLeaves.clear
    root.children.clear
    branches += (root.pos -> root)
    growIteration = 0
    viewIteration = 0
    dirty = true
  }


  def grow(){

    if (leaves.size == 0) { 
        return
    }

    try{
    //process the leaves
    var i = 0
    // while( i < leaves.size){
    leaves.foreach{ case leaf => 

      var leafRemoved = false

      var direction = Vec3()
      // val leaf = leaves(i)
      leaf.closest = null

      //Find the nearest branch for this leaf
      var break = false
      val near = branches.getInSphere(leaf.pos, maxDistance)
      near.values.foreach( (b) => { 
        if(!break){
          direction = leaf.pos - b.pos
          val dist = direction.mag
          direction.normalize

          if( dist <= minDistance){
            leaves -= leaf
            i -= 1
            leafRemoved = true
            break = true
          } else if( dist <= maxDistance){
            if( leaf.closest == null)
              leaf.closest = b 
            else if ( (leaf.pos - leaf.closest.pos).mag > dist)
              leaf.closest = b
          }
        }
      })

      //if the leaf was removed, skip
      if (!leafRemoved){
          //Set the grow parameters on all the closest branches that are in range
          if (leaf.closest != null){
              val dir = leaf.pos - leaf.closest.pos
              dir.normalize()
              leaf.closest.growDirection += dir       //add to grow direction of branch
              leaf.closest.growCount += 1
          }
      }

      i += 1
    }

    //Generate the new branches
    val newBranches = HashSet[Branch]()

    branches.foreach( (p,b) => {
      if (b.growCount > 0){    //if at least one leaf is affecting the branch
      
          val avgDirection = b.growDirection / b.growCount
          avgDirection.normalize()

          // avgDirection.mag() / branchLength
          val newBranch = new Branch(b, b.pos + avgDirection * branchLength, avgDirection);
          // b.grow()
          // b.children += newBranch

          newBranches += newBranch
          b.reset()
      }
    })
    if(newBranches.size > 0) growIteration += 1

    //Add the new branches to the tree
    var branchAdded = false;
    newBranches.foreach( (b) => {
      // if (!branches.values.contains(b.pos)){
        b.parent.grow()
        b.parent.children += b
        branches += (b.pos -> b)
        branchAdded = true
      // }
    })

    } catch { case e:Exception => println(e.getMessage)}

  }

  def trim() = {
    //process the leaves
    var i = 0
    val trimmed = ListBuffer[Branch]()

    def removeChildren(b:Branch){
      if( b == root) return
      branches.remove(b.pos, b)
      b.children.foreach(removeChildren(_))
    }

    antiLeaves.foreach( (leaf) => {

      val mag = leaf.vel.mag()

      if( mag > thresholdVel && mag < thresholdVelMax ){
        var direction = Vec3()

        //Find the nearest branch for this leaf
        var break = false
        var broken:Branch = null
        val near = branches.getInSphere(leaf.pos, trimDistance)
        near.values.foreach( (b) => {  //TODO remove all children of removed as well
          // if(!break){
          //   direction = leaf.pos - b.pos
          //   val dist = direction.mag
          //   direction.normalize

          //   if( dist <= maxDistance && b != root){
          //     break = true
          //     broken = b                
          //   }
          // }
          if( b != root){
            removeChildren(b)
            // branches.remove(b.pos, b)
            b.parent.children -= b
            b.parent = null
            trimmed += b
            dirty = true
          }

        }) 

        // if(broken != null){
          // branches.remove(broken.pos, broken)
          // broken.parent.children -= broken
          // broken.parent = null
          // trimmed += broken
        // }
      }
    })
    trimmed
  }
}

class TreeGrowActor(tree:Tree) extends Actor with ActorLogging {
  var running = false
  def receive = {
    case "start" => running = true; self ! "update"
    case "update" => if(running){ tree.grow(); tree.dirty = true; Thread.sleep(tree.sleep); self ! "update" }
    case "stop" => running = false;
    case _ => ()
  }
     
  override def preStart() = {
    log.debug("TreeGrow actor Starting")
  }
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "TreeGrow actor Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }
}



