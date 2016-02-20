#include "exceptions.h"

#include <Windows.h>

namespace windowscommon {

DebugExceptionContainer exception_list = {
    DebugException("Access violation", EXCEPTION_ACCESS_VIOLATION, HALT),
    DebugException("Illegal instruction", EXCEPTION_ILLEGAL_INSTRUCTION, HALT),
    DebugException("Privileged instruction", EXCEPTION_PRIV_INSTRUCTION, HALT),
    DebugException("Integer division by zero", EXCEPTION_INT_DIVIDE_BY_ZERO,
                   HALT),
    DebugException("Integer overflow", EXCEPTION_INT_OVERFLOW, HALT),
    DebugException("Stack overflow", EXCEPTION_STACK_OVERFLOW, HALT),
    DebugException("Guard page", EXCEPTION_GUARD_PAGE, HALT),
    DebugException("Non-continuable exception",
                   EXCEPTION_NONCONTINUABLE_EXCEPTION, HALT),
    DebugException("Floating point division by zero",
                   EXCEPTION_FLT_DIVIDE_BY_ZERO, HALT),
    DebugException("Floating point invalid operation",
                   EXCEPTION_FLT_INVALID_OPERATION, HALT),
    DebugException("Invalid handle closed", EXCEPTION_INVALID_HANDLE, HALT),
    DebugException("Microsoft Visual C++ Exception", 0xE06D7363, PASS_TO_APP),
    DebugException("IE Internal Communication Exception", 0x406D1388, PASS_TO_APP),
    DebugException("Interface not registered. REGDB_E_IIDNOTREG", 0x80040155,
                   PASS_TO_APP),
    DebugException(
        "The remote procedure call was canceled. RPC_S_CALL_CANCELLED", 0x71A,
        PASS_TO_APP)};

}  // namespace windowscommon
