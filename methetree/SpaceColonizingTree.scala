
package com.fishuyo.seer
package spacetree

import spatial._
import graphics._

import collection.mutable.ListBuffer
import collection.mutable.HashMap
import collection.mutable.HashSet


class Branch(var parent:Branch, var pos:Vec3, var growDirection:Vec3){
  var growDirection0 = Vec3(growDirection)
  var growCount = 0
  var age = 0

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


class Tree {
  var done = false
  var pos = Vec3()

  // var leafCount = 400
  var minDistance = 0.05
  var maxDistance = 0.1 //0.35
  var branchLength = 0.04
 
  var root = new Branch(null, Vec3(), Vec3(0,1,0))
  var leaves = ListBuffer[Leaf]()
  // var branches = HashMap[Vec3,Branch]()

  var branches = Octree[Branch](Vec3(0),5)

  branches += (root.pos -> root)
  // branches += (Vec3(0,0.1,0) -> new Branch(root, Vec3(0,0.1,0), Vec3(0,1,0)))

  // for( i <- (0 until leafCount))
    // leaves += new Leaf(Random.vec3())

  def reset(){
    branches.clear
    leaves.clear
    branches += (root.pos -> root)
    // branches += (Vec3(0,0.1,0) -> new Branch(root, Vec3(0,0.1,0), Vec3(0,1,0)))
  }

  def grow(){

    if (leaves.size == 0) { 
        return
    }

    //process the leaves
    var i = 0
    while( i < leaves.size){

      var leafRemoved = false

      var direction = Vec3()
      val leaf = leaves(i)
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
    // branches.getAll().values.foreach( (b) => {
    branches.foreach( (p,b) => {
      if (b.growCount > 0){    //if at least one leaf is affecting the branch
      
          val avgDirection = b.growDirection / b.growCount
          avgDirection.normalize()

          // avgDirection.mag() / branchLength
          val newBranch = new Branch(b, b.pos + avgDirection * branchLength, avgDirection);
          b.grow()

          newBranches += newBranch
          b.reset()
      }
    })

    //Add the new branches to the tree
    var branchAdded = false;
    newBranches.foreach( (b) => {
      // if (!branches.values.contains(b.pos)){
        branches += (b.pos -> b)
        branchAdded = true
      // }
    })
    

  }
}



