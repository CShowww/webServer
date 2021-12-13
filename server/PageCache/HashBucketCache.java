package com.cangjian.server.PageCache;

public class HashBucketCache {
    public static final int length = 128;//the length of arr（the number of key is 128
    public static final int listLength = 5;//the length of each arr's list
    public HashTab[] hashTab = new HashTab[length];
    public HashBucketCache(){
        for (int i = 0; i < length; i++) {
            hashTab[i] = new HashTab(null);
        }
    }

    //structure of hashmap
    public static class HashTab{
        public int number = 0;//the number of HashPair in the current hashmap
        public HashPair head;
        public HashTab(HashPair head){this.head = head;}
    }

    //structure of hashpair
    public static class HashPair{
        public String name;//key
        public byte[] content;//value
        public int length;
        public int count = 0;//used times of current file
        public HashPair next;
        public HashPair(String name,byte[] content,int length){
            this.name = name;
            this.content = content;
            this.length = length;
            this.next = null;
        }
    }

    public static int hash(String key){
        int h;
        return (h = key.hashCode()) ^ (h >>> 16);
    }

    public HashPair getFileCacheByName(String fileName){
        int hashNum = hash(fileName);
        int i = (length-1)&hashNum;//get the location of the file
        if(hashTab[i].number == 0){
            return null;
        }

        //there exists at least one element in the hashtab
        HashPair pair = hashTab[i].head;
        while(pair != null){
            if(pair.name.equals(fileName)){
                System.out.println("The file was got from the cache.");
                return pair;
            }
            pair = pair.next;
        }

        return null;//there is no cache
    }

    public void put(String fileName,byte[] content,int contentLength){
        putVal(hash(fileName),fileName,content,contentLength);
    }

    public synchronized void putVal(int hash,String key,byte[] content,int contentLength){
        HashPair p;
        int i = (length-1)&hash;//get the location of the file we want to put
        if((p = hashTab[i].head) == null)
        {
            System.out.println("Put the "+key+"into the cache.");
            HashPair node = new HashPair(key,content,contentLength);
            node.count = 1;
            hashTab[i] = new HashTab(node);//make the node as the head
            hashTab[i].number = 1;//the total number of hashpair in the location of i is 1.
        }
        else if (hashTab[i].number < listLength){
            //first,we need to find that if the file exists in the cache
            //if the file has been put into the cache,we only need to increase the number
            //or we need to put the file into the tail of the list

            HashPair pair = hashTab[i].head;
            HashPair pre = null;

            while(pair != null){
                if(pair.name.equals(key)){
                    System.out.println("This file has been in the cache.");
                    pair.count += 1;

                    if(pre != null){
                        pre.next = pair.next;
                    }
                    else{
                        hashTab[i].head = pair.next;
                    }
                    break;
                }
                pre = pair;
                pair = pair.next;
            }
            if(pair == null){
                System.out.println("Put the "+key+"into the cache.");
                HashPair now = new HashPair(key,content,contentLength);
                now.count = 1;
                pre.next = now;
                hashTab[i].number += 1;
            }
            else{
                while(pre.next == null){
                    pre.next = pair;
                    pair.next = null;
                    pre = pre.next;
                }
            }
        }

        //if the current list is not null and the number of node is more than 5,we need to find if the file we want to put into the cache exists
        //if the file has been put into the cache,we only need to increase the number
        //or we need to replace the page by some algorithms
        else{
            HashPair pair = hashTab[i].head;
            HashPair pre = null;
            while(pair != null){
                if(pair.name.equals(key)){
                    System.out.println("This file has been in the cache.");
                    pair.count += 1;

                    if(pre != null){
                        pre.next = pair.next;
                    }
                    else{
                        hashTab[i].head = pair.next;
                    }
                    break;
                }
                pre = pair;
                pair = pair.next;

            }

            if(pair == null){
                System.out.println("LRU PAGE REPLACEMENT!");
                LRU(i,key,content);
            }
            else{
                while(pre.next != null){
                    pre = pre.next;
                }
                pre.next = pair;
                pair.next = null;
            }

        }

    }

    public void LRU(int i,String key,byte[] content){
        hashTab[i].head.name = key;
        hashTab[i].head.content = content;
        hashTab[i].head.count = 1;
    }

}
