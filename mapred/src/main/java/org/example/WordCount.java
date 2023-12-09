package org.example;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class WordCount {

    public static class CustomMapper
            extends Mapper<Object, Text, Text, Text>{

        private final static IntWritable one = new IntWritable(1);



        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] cols = value.toString().split(",");

            int cases = 0;
            int deaths = 0;
            try{
                cases = Integer.parseInt(cols[5]);
            } catch (RuntimeException nxe){}

            try {
                deaths = Integer.parseInt(cols[8]);
            } catch (NumberFormatException|IndexOutOfBoundsException e){

            }

            context.write(new Text(cols[0]), new Text(cases + "\t\t" + deaths + "\t\t0"));
            System.out.println((double)cases + "/" + (double)deaths);
        }
    }

    public static class CustomReducer
            extends Reducer<Text,Text,Text, Text> {
        private Text result = new Text();

        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {
            double sumCases = 0;
            double sumDeath = 0;
            double res = 0;
            for (Text val : values) {
                try{
                    sumCases += Double.parseDouble(val.toString().split("\t\t")[0]);
                    sumDeath += Double.parseDouble(val.toString().split("\t\t")[1]);
                } catch (Exception e){
                    throw new RuntimeException("String:\n" + val.toString() + "\n" + e);
                }


                if (sumDeath == 0){
                    res = 0;
                } else {
                    res = sumCases/sumDeath;
                }


            }
            result.set(sumCases+"\t\t"+sumDeath+"\t\t"+res);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "Covid-19 Cases/Death Rate");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(CustomMapper.class);
        job.setCombinerClass(CustomReducer.class);
        job.setReducerClass(CustomReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }


}