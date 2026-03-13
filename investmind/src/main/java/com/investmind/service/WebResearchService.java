package com.investmind.service;

public interface WebResearchService {

    /**
     * 联网检索问题相关信息，返回可直接拼接到提示词的摘要文本。
     */
    String research(String question);
}

