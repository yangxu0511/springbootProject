package com.study.common.controller;

import com.study.common.model.Dish;
import com.study.common.model.TestModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("hello")
public class HelloController {

    @ResponseBody
    @RequestMapping("/hello")
    public  String  hello(){
        return "hello world";
    }



    public static void main(String[] args) {

        //Lambda 表达式
        // File[] hiddenFiles = new File(".").listFiles(File::isHidden);


        Runnable r1 = () -> System.out.println("Hello World 1");
        Consumer<String> stringConsumer = (String s) -> System.out.println(s);
        Consumer<String> stringConsumer2 = System.out::println;

        List<String> str = Arrays.asList("a","b","A","B");
        str.sort((s1, s2) -> s1.compareToIgnoreCase(s2));
        //上下两个都可以
        str.sort(String::compareToIgnoreCase);


        Function<String, Integer> stringToInteger =
                (String s) -> Integer.parseInt(s); //Lambda表达式
        Function<String, Integer> stringToInteger2 = Integer::parseInt; //Lambda方法引用

        BiPredicate<List<String>, String> contains =
                (list, element) -> list.contains(element);

            BiPredicate<List<String>, String> contains2 = List::contains;


        Supplier<TestModel> models = TestModel::new;

        Supplier<TestModel> models2 = () -> new TestModel();

        String s = "acb";
        s.toLowerCase();



        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        //获取数组每个数的平方
        List<Integer> numbers2 =  numbers.stream()
                                  .map((n) -> n*n)
                                  .collect(toList());
        numbers2.forEach(System.out::println);









        testStreamApi();
    }



    public static void testStreamApi(){
        List<Dish> menu = Arrays.asList(
                new Dish("pork", false, 800, Dish.Type.MEAT),
                new Dish("beef", false, 700, Dish.Type.MEAT),
                new Dish("chicken", false, 400, Dish.Type.MEAT),
                new Dish("french fries", true, 530, Dish.Type.OTHER),
                new Dish("rice", true, 350, Dish.Type.OTHER),
                new Dish("season fruit", true, 120, Dish.Type.OTHER),
                new Dish("pizza", true, 550, Dish.Type.OTHER),
                new Dish("prawns", false, 300, Dish.Type.FISH),
                new Dish("salmon", false, 450, Dish.Type.FISH) );


        List<Dish> vegetarianList =
        menu.stream()
                .filter(Dish::isVegetarian)
                .collect(toList());


        //获取所有Dish name品种
        List<String> names = menu.stream()
                .map((m) -> m.getName())
                .distinct()
                .collect(toList());

        names.forEach(System.out::println);

        vegetarianList.forEach(System.out::println);

    }



}
