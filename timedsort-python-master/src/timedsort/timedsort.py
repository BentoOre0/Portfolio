from time import perf_counter
from functools import wraps
from random import randint


def help_me():
    print("================================================================================================")
    print("About:")
    print("timedsort is a python module made for educational purposes")
    print("it has 6 sorting algorithms that can be timed and compared easily")
    print("This is version 0.0.3 of timedsort")
    print("================================================================================================")
    print("This module calls the following built in libraries:")
    print("----time: perf_counter")
    print("----random: randint")
    print("----functools: wraps")
    print("----itertools: permutations (bogo sort)")
    print("================================================================================================")
    print("Generation:")
    print("----gen_ran_int => returns list with random integers.")
    print("----arguments format => length,range_start,range_end")
    print("----standard python ranges apply (i.e RANGES DO NOT GO TO THE LAST VALUE)")
    print("================================================================================================")
    print("SORTING ALGORITHMS:")
    print("--1) each of the following functions print the time taken")
    print("--2) arguments should be list type")
    print("--3) the kwarg out => displays list before and after sorting, default is True")
    print("----if set to False only time is displayed. This is faster.")
    print("--methods/functions:")
    print("----bubble => bubble sort")
    print("----selection => selection sort")
    print("----insertion => insertion sort")
    print("----merge => merge sort")
    print("----heap => heap sort, the algorithm used here was made by geek for geeks")
    print("----quick => quick sort")
    print("================================================================================================")
    print("Complexities:")
    print("----BIGO => prints BIGO notation for sorting algorithms, args => string name of sorting algorithm")
    print("================================================================================================")
    print("Others:")
    print("--using these methods as arguments for BIGO will result in a printed error message")
    print("----timer => this is a decorator used to time code")
    print("----heapify => a function to create a heap structure from an array, uses recursion.")
    print("----bogo => bogo sort (meme): algorithm iterates over each permutation until array is sorted")
    print("----miracle => miracle sort (meme): an endless prayer for a miracle to stop somehow sort the array")
    print("----builtin => just shows how much faster builtins are")
    print("----partition => used to partition and sort array in quick sort")
    print("================================================================================================")
    print("Notes:")
    print("----There are also functions with an underscore behind them. They are the raw algorithms")
    print("----Calling the these functions will change the actual list and not print anything.")
    print("----using these functions as arguments for BIGO will result in a printed error message")
    print("================================================================================================")
    print("Developer: Jeremy Yu")
    print("Appendices: Vince Tiu, Geek for Geeks, Stack Overflow, mycodeschool - Youtube, Abdul Bari")
    print("================================================================================================")


def timer(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start = perf_counter()
        result = func(*args, **kwargs)
        end = perf_counter()
        elapsed = end - start
        print(f"{func.__name__} sort time was: {elapsed} seconds")
        print()
        return result

    return wrapper


def BIGO(args):
    args = args.lower()
    BIG_O_dictionary = {"bubble": ["bubble sort:", "BEST: O(n)", "AVE: O(n^2)", "WORST: O(n)"],
                        "insert": ['insertion sort:', 'BEST: O(n)', 'AVE: O(n^2)', 'WORST: O(n)'],
                        "selection": ['selection sort:', 'BEST: O(n)', 'AVE: O(n^2)', 'WORST: O(n)'],
                        "merge": ['merge sort:', 'BEST: O(n log(n))', 'AVE: O(n log(n))', 'WORST: O(n log(n))'],
                        "heap": ['heap sort:', 'BEST: O(n log(n))', 'AVE: O(n log(n))', 'WORST: O(n log(n))'],
                        "quick": ['quick sort:', 'BEST: O(n log(n))', 'AVE: O(n log(n))', 'WORST: O(n^2)']}
    try:
        print(*BIG_O_dictionary[args], sep='\n')
    except KeyError:
        print("ERROR sorting method not found")


def gen_ran_int(length, range1=None, range2=None):
    if range1 is None:
        range1 = 0
    if range2 is None:
        range2 = 2 * length
    array = []
    for i in range(length):
        array.append(randint(range1, range2))
    return array


@timer
def bubble(array, out=True):
    if out is True:
        print(array)
    args = array.copy()  # we create a copy of the array, allows us to reuse array
    _bubble(args)  # this is the raw algorithm
    if out is True:
        print(args)


def _bubble(args):  # raw algorithm
    swap = True
    while swap is True:
        swap = False
        for i in range(len(args) - 1):  # loop repeats over the array
            if args[i] > args[i + 1]:  # checks if current which if next is bigger
                args[i], args[i + 1] = args[i + 1], args[i]  # swaps them
                swap = True  # swap has occurred
        # loop repeats


@timer
def selection(array, out=True):
    if out is True:
        print(array)
    args = array.copy()
    _selection(args)
    if out is True:
        print(args)


def _selection(args):
    length = len(args)  # prevent unnecessary repeated calculations
    for i in range(length):  # for loop through all elements to  find min of subarray
        mindex = i  # sets minimum index of subarray as the first element
        for j in range(i + 1, length):
            if args[mindex] > args[j]:
                mindex = j  # reassigns value if new minimum found
        args[i], args[mindex], = args[mindex], args[
            i]  # swaps the minimum of subarray with the first position of subarray


@timer
def insertion(array, out=True):
    if out is True:
        print(array)
    args = array.copy()
    _insertion(args)
    if out is True:
        print(args)


def _insertion(args):
    for i in range(1, len(args)):  # going to compare a current element with element before it
        hole = args[i]  # hole is value taken out from the array to allow all the other elements to shift
        # don't actually delete the hole
        j = i  # we want to iterate indefinitely
        while hole < args[j - 1] and j > 0:  # repeated until the hole finds its spot in array.
            # second condition is used to prevent negative indexes
            args[j] = args[j - 1]  # shift elements greater than hole 1 to the right
            j -= 1
        args[j] = hole  # hole finds its spot and is inserted


@timer
def merge(array, out=True):
    args = array.copy()
    if out is True:
        print(array)
    _merge(args)
    if out is True:
        print(args)


def _merge(args):
    length = len(args)
    if length > 1:
        mid = length // 2  # integer division as indexes can only be integers
        left = args[:mid]
        right = args[mid:]
        _merge(left)
        _merge(right)
        # assume left and right arrays are already sorted
        L = len(left)
        R = len(right)
        minL = 0  # minimum of left subarray
        minR = 0  # minimum of right subarray
        mainarr = 0  # index of main array before any splitting or merging
        while minL < L and minR < R:
            if left[minL] <= right[minR]:  # checks which is smaller minimum between subarrays
                args[mainarr] = left[minL]  # the current position in main array is set to the minimum
                minL += 1  # next unpicked position in left array
            else:
                args[mainarr] = right[minR]  # the current position in main array is set to the minimum
                minR += 1  # next unpicked position in right array
            mainarr += 1
        # in some cases, 1 subarray may be exhausted first. So we need to check if there are any left
        while minL < L:  # check left remaining
            args[mainarr] = left[minL]
            minL += 1
            mainarr += 1
        while minR < R:  # check right remaining
            args[mainarr] = right[minR]
            minR += 1
            mainarr += 1


# HEAPSORT CREDIT goes to geek for geeks, I am not proficient at binary trees yet.
def heapify(arr, N, i):
    largest = i  # Initialize largest as root
    l = 2 * i + 1  # left = 2*i + 1
    r = 2 * i + 2  # right = 2*i + 2

    # See if left child of root exists and is
    # greater than root
    if l < N and arr[largest] < arr[l]:
        largest = l

    # See if right child of root exists and is
    # greater than root
    if r < N and arr[largest] < arr[r]:
        largest = r

    # Change root, if needed
    if largest != i:
        arr[i], arr[largest] = arr[largest], arr[i]  # swap

        # Heapify the root.
        heapify(arr, N, largest)


@timer
def heap(array, out=True):
    if out is True:
        print(array)
    arr = array.copy()
    _heap(arr)
    if out is True:
        print(arr)
    print("CREDIT TO GEEK FOR GEEKS.")


def _heap(arr):
    # The main function to sort an array of given size
    N = len(arr)

    # Build a maxheap.
    for i in range(N // 2 - 1, -1, -1):
        heapify(arr, N, i)

    # One by one extract elements
    for i in range(N - 1, 0, -1):
        arr[i], arr[0] = arr[0], arr[i]  # swap
        heapify(arr, i, 0)


def partition(array, low, high):
    pivot = array[low]
    i = low + 1
    j = high
    while True:
        while i <= j and array[j] >= pivot:
            j -= 1
        # stops until it finds something bigger than the pivot
        while i <= j and array[i] <= pivot:
            i += 1  # stops until it finds something lower than the pivot
        if i <= j:
            array[i], array[j] = array[j], array[i]
        else:
            break
    array[low], array[j] = array[j], array[low]  # swap once all other elements are sorted
    return j  # returns the position of the pivot


@timer
def quick(array, low=0, high=None, out=True):
    if out is True:
        print(array)
    args = array.copy()
    if high is None:
        high = len(args) - 1
    _quick(args, low, high)
    if out is True:
        print(args)


def _quick(args, low=0, high=None):
    if low >= high:
        return
    j = partition(args, low, high)  # sorts the elements and returns position of pivot
    _quick(args, low, j - 1)
    _quick(args, j + 1, high)


@timer
def bogo(array, out=True):
    if out is True:
        print(array)
    args = array.copy()
    _bogo(args)
    if out is True:
        print(args)


def _bogo(args):
    # CAUTION USE AT YOUR OWN RISK
    from itertools import permutations
    for elem in permutations(args):
        condition = True
        for i in range(len(elem) - 1):
            if elem[i] > elem[i + 1]:
                condition = False
                break
        if condition is True:
            return


@timer
def builtin(array, out=True):
    if out is True:
        print(array)
    args = array.copy()
    args.sort()
    if out is True:
        print(args)


@timer
def miracle(array, out=True):
    if out is True:
        print(array)
    args = array.copy()
    _miracle(args)
    if out is True:
        print(args)


def _miracle(args):
    issorted = False
    while issorted is False:
        issorted = True
        for i in range(1, len(args)):
            if args[i] < args[i - 1]:
                issorted = False
