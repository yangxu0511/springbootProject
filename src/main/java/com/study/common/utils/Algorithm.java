package com.study.common.utils;

/**
 * 力扣算法学习
 */
public class Algorithm {
    /**
     * 1.两数之和
     * 给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那 两个整数，并返回他们的数组下标。
     * 你可以假设每种输入只会对应一个答案。但是，你不能重复利用这个数组中同样的元素。
     */

    public static void main(String[] args) {
        int[] nums = {1, 4, 5, 6, 8};
        int[] result = calculateFormula(nums, 14);
        System.out.println("第一个下标的值：" + result[0] + "，第二个下标的值：" + result[1]);
    }

    public static int[] calculateFormula(int[] nums, int target) {
        int[] result = new int[2];
        for (int i = 0; i < nums.length - 1; i++) {
            for (int j = 0; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    result[0] = i;
                    result[1] = j;
                }
            }
        }
        return result;
    }

}
