package VM;

/*
  Instruction codes of the virtual machine
*/

public enum OpCode {
    // Instructions with 1 argument (5 bytes)
    iconst(1),      // 0
    dconst(1),      // 1
    sconst(1),      // 2
    jump(1),        // 41
    jumpf(1),       // 42
    galloc(1),      // 43
    gload(1),       // 44
    gstore(1),      // 45
    lalloc(1),      // 46
    lload(1),       // 47
    lstore(1),      // 48
    pop(1),         // 49
    call(1),        // 50
    retval(1),      // 51
    ret(1),         // 52

    // Instructions with no arguments (1 byte)
    iprint(0),      // 3
    iuminus(0),     // 4
    iadd(0),        // 5
    isub(0),        // 6
    imult(0),       // 7
    idiv(0),        // 8
    imod(0),        // 9
    ieq(0),         // 10
    ineq(0),        // 11
    ilt(0),         // 12
    ileq(0),        // 13
    itod(0),        // 14
    itos(0),        // 15
    dprint(0),      // 16
    duminus(0),     // 17
    dadd(0),        // 18
    dsub(0),        // 19
    dmult(0),       // 20
    ddiv(0),        // 21
    deq(0),         // 22
    dneq(0),        // 23
    dlt(0),         // 24
    dleq(0),        // 25
    dtos(0),        // 26
    sprint(0),      // 27
    sconcat(0),     // 28
    seq(0),         // 29
    sneq(0),        // 30
    tconst(0),      // 31
    fconst(0),      // 32
    bprint(0),      // 33
    beq(0),         // 34
    bneq(0),        // 35
    and(0),         // 36
    or(0),          // 37
    not(0),         // 38
    btos(0),        // 39
    halt(0)         // 40
    ;

    private final int nArgs;    // number of arguments

    OpCode(int nArgs) {
        this.nArgs = nArgs;
    }
    public int nArgs() { return nArgs; }

    // convert byte value into an OpCode
    public static OpCode convert(byte value) {
        return OpCode.values()[value];
    }
}
