# timedsort-python
## About
timedsort is a python module that allows new coders to see the time differences in a variety of sorting algorithms. Although many people use languages such as C++ for algorithmic design, it is notorious for having a steep learning curve. Python on the otherhand is more beginner friendly. With that in mind, this module provides the ability for people to see the relative differences of sorting algorithm speeds through time.

**You may call help_me() to see a more detailed description!**

## Builtin Libraries
timesort uses the following builtin libraries:
1. time: perf_counter (timer decorator)
2. random: randint (gen_ran_int)
3. functools: wraps (timer decorator)
4. itertools: permutations (bogo sort)

## SORTING ALGORITHMS
timedsort allows you to time the following sorting algorithms
1. bubble sort
2. selection sort
3. insertion sort
4. merge sort
5. heap sort
6. quick sort
7. call help_me() to see the meme sorts!

## How to use
- generate a list of integers we can use the gen_ran_int() function!
- parameters format: (length, range1, range2)
- if range1 and range2 are ommitted, the range would automatically from 0 to twice the length

e.g.

`import timedsort as ts`

`arr = ts.gen_ran_int(10)`

`#[7, 24, 35, 28, 36, 37, 2, 10, 20, 0, 18, 28, 0, 0, 34, 30, 8, 19, 6, 29]`

`arr2 = ts.gen_ran_int(5,-10,10)`

`#[-7, -1, -1, 5, 10]`

You may then call a sorting function to be timed!

`ts.bubble(arr)`

outputs:

`[7, 24, 35, 28, 36, 37, 2, 10, 20, 0, 18, 28, 0, 0, 34, 30, 8, 19, 6, 29]`

`[0, 0, 0, 2, 6, 7, 8, 10, 18, 19, 20, 24, 28, 28, 29, 30, 34, 35, 36, 37]`

`bubble sort time was: 0.00010280001151841134 seconds`

or

`ts.bubble(arr, out = False)`

outputs:

`bubble sort time was: 7.199999527074397e-05 seconds`

As one can see, it is faster because the program doesn't print the list before and after sorting. When comparing algorithms, make sure that this kwarg is the same throughout the program.

## More Examples:
`ts.bubble(arr, out = False)`

`ts.quick(arr, out = False)`

outputs:

`bubble sort time was: 8.349999552592635e-05 seconds`


`quick sort time was: 4.939999780617654e-05 seconds`

## Notes
If you would like to mutate the actual list, add an underscore before the function you call! This calls the raw algorithm used to sort the list! Make sure that the argument placed for these functions is 1 list. It also does't display the time taken.

suppose we have the following code:

`print(arr)`

`ts._selection(arr)`

`print(arr)`

`ts._heap(arr)`

`print(arr)`

outputs:

`[7, 24, 35, 28, 36, 37, 2, 10, 20, 0, 18, 28, 0, 0, 34, 30, 8, 19, 6, 29]`

`[0, 0, 0, 2, 6, 7, 8, 10, 18, 19, 20, 24, 28, 28, 29, 30, 34, 35, 36, 37]`

`[0, 0, 0, 2, 6, 7, 8, 10, 18, 19, 20, 24, 28, 28, 29, 30, 34, 35, 36, 37]`

This actually changes the list.

## Complexities
One may call on the BIGO function to see an algorithm's time complexity!

e.g.

`ts.BIGO("bubble")`

`ts.BIGO("Merge")`

It is not case sensitive!

outputs:

`bubble sort:`

`BEST: O(n)`

`AVE: O(n^2)`

`WORST: O(n)`

`merge sort:`

`BEST: O(n log(n))`

`AVE: O(n log(n))`

`WORST: O(n log(n))`

## Appendices:

- Vince Tiu
- Geek for Geeks
- Stack Overflow
- mycodeschool
- Abdul Bari

Hope you enjoy!
