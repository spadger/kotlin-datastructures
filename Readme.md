# What is this?

Just a playground for stuff I want to learn in my spare time

[![Java CI with Gradle](https://github.com/spadger/kotlin-datastructures/actions/workflows/gradle.yml/badge.svg)](https://github.com/spadger/kotlin-datastructures/actions/workflows/gradle.yml)

## Contents:

## Tries
[Tries](https://en.wikipedia.org/wiki/Trie) are compact datastructures to allow you to work out if some text exists in a larger string

## Huffman coder
 [Huffman Coding](https://en.wikipedia.org/wiki/Huffman_coding) is a Simple method of compressing data.
 
Each distinct byte found in the input is represented by a variable length set of bits, so e.g. if `0XB1` is really common, it could be represented by the bit string `0`. 
If `0xA2` is really rare, it may be represented by the bit-string `101001010111` - much longer than 1-byte   
 
There are more optimal compression algorithms, but this one is simple enough to be completed in a few hours
  
This code is an inefficient implementation because it stores the entire uncompressed input in memory.
In a better implementation, there would be 2 phases:
 1. Stream in the data, building up the histogram needed to create a huffman tree
 2. Stream the data in again, this time using it to stream out the compress binary stream

I just wanted to make a working toy