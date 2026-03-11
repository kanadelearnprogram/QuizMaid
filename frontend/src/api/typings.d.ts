declare namespace API {
  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseInteger = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseListUserHeatMapVO = {
    code?: number
    data?: UserHeatMapVO[]
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseMapLocalDateBoolean = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserLoginVO = {
    code?: number
    data?: UserLoginVO
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type BindEmailDTO = {
    email?: string
    code?: string
  }

  type callbackParams = {
    code: string
  }

  type DeleteRequest = {
    id?: number
  }

  type getSignInDaysParams = {
    userId: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserSignDataParams = {
    year: number
  }

  type getUserVoByIdParams = {
    id: number
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type ResetPasswordDTO = {
    password?: string
    email?: string
    code?: string
  }

  type sendEmailParams = {
    email: string
  }

  type User = {
    id?: number
    username?: string
    password?: string
    nickname?: string
    email?: string
    role?: string
    emailVerified?: number
    oauthType?: string
    oauthOpenid?: string
    status?: number
    answerNum?: number
    correctNum?: number
    createTime?: string
    updateTime?: string
    isDeleted?: number
  }

  type UserAddDTO = {
    username?: string
    nickname?: string
    role?: string
  }

  type UserHeatMapVO = {
    date?: string
    count?: number
    level?: number
  }

  type UserLoginByEmailDTO = {
    email?: string
    code?: string
  }

  type UserLoginDTO = {
    username?: string
    userPassword?: string
  }

  type UserLoginVO = {
    id?: number
    username?: string
    password?: string
    nickname?: string
    email?: string
    role?: string
    emailVerified?: number
    oauthType?: string
    oauthOpenid?: string
    status?: number
    answerNum?: number
    correctNum?: number
    createTime?: string
    updateTime?: string
  }

  type UserQueryDTO = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    nickname?: string
    username?: string
    role?: string
  }

  type UserRegisterByEmailDTO = {
    userName?: string
    userPassword?: string
    checkUserPassword?: string
    email?: string
    code?: string
  }

  type UserRegisterDTO = {
    userName?: string
    userPassword?: string
    checkUserPassword?: string
  }

  type UserUpdateByAdminDTO = {
    id?: number
    username?: string
    nickname?: string
    email?: string
    role?: string
    emailVerified?: number
    status?: number
    answerNum?: number
    correctNum?: number
  }

  type UserUpdateDTO = {
    id?: number
    nickname?: string
    email?: string
  }

  type UserVO = {
    id?: number
    username?: string
    nickname?: string
    email?: string
    role?: string
    emailVerified?: number
    oauthType?: string
    oauthOpenid?: string
    status?: number
    answerNum?: number
    correctNum?: number
    createTime?: string
    updateTime?: string
  }
}
